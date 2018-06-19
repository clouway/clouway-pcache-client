package com.clouway.api.pcache.testing;

import com.clouway.api.pcache.CacheException;
import com.clouway.api.pcache.CacheManager;
import com.clouway.api.pcache.MissedHitsProvider;
import com.clouway.api.pcache.MatchResult;
import com.clouway.api.pcache.SafeValue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * @author Miroslav Genov (mgenov@gmail.com)
 */
public class InMemoryCacheManager implements CacheManager {

  class TimeValue {
    public final Integer seconds;
    public final Object value;

    public TimeValue(Integer seconds, Object value) {
      this.seconds = seconds;
      this.value = value;
    }
  }

  class CompareAndSwapSafeValue implements SafeValue{
    private final Object value;

    CompareAndSwapSafeValue(Object value) {
      this.value = value;
    }

    @Override
    public Object getValue() {
      return value;
    }
  }

  private Map<Object, TimeValue> values = new HashMap<Object, TimeValue>();

  public void put(String key, Object value, Integer seconds) {
    put(key, value, seconds * 1000l);
  }

  @Override
  public void put(String key, Object value, Long mills) {
    if (!(value instanceof Serializable)) {
      throw new CacheException("You are trying to store non serializable object in cache which is not allowed.");
    }

    values.put(key, new TimeValue(0, value));
  }

  public void put(String key, Object value) {
    if (!(value instanceof Serializable)) {
      throw new CacheException("You are trying to store non serializable object in cache which is not allowed.");
    }
    values.put(key, new TimeValue(0, value));
  }

  public Object get(String key) {
    if (!values.containsKey(key)) {
      return null;
    }
    return values.get(key).value;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> MatchResult<V> getAll(List<String> keys, Class<V> clazz) {
    List<V> hits = new LinkedList<>();
    List<String> missed = new LinkedList<>();

    for(String key: keys) {
      TimeValue timeValue = values.get(key);

      if(timeValue != null) {
        try {
          hits.add(clazz.cast(timeValue.value));
        } catch(ClassCastException e) {
          missed.add(key);
        }
      } else {
        missed.add(key);
      }
    }

    return new MatchResult(hits, missed);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> List<T> getAll(List<String> keys, Class<T> clazz, MissedHitsProvider<T> missedHitsProvider) {
    List<T> output = new LinkedList<>();
    MatchResult result = getAll(keys, clazz);

    output.addAll(result.getHits());
    output.addAll(missedHitsProvider.get(result.getMissedKeys()));

    return output;
  }

  public void remove(String key) {
    values.remove(key);
  }

  @Override
  public boolean safePut(Object key, SafeValue safeValue, Object value) {
    CompareAndSwapSafeValue compareAndSwapSafeValue = (CompareAndSwapSafeValue) safeValue;
    TimeValue existingValue = values.get(key);

    if (compareAndSwapSafeValue.getValue().equals(existingValue.value)) {
      values.put(key, new TimeValue(0, value));
      return true;
    }

    return false;
  }

  @Override
  public boolean safePut(Object key, SafeValue safeValue, Object value, Integer expiration) {
    CompareAndSwapSafeValue compareAndSwapSafeValue = (CompareAndSwapSafeValue) safeValue;
    TimeValue existingValue = values.get(key);

    if (compareAndSwapSafeValue.getValue().equals(existingValue.value)) {
      values.put(key, new TimeValue(expiration, value));
      return true;
    }

    return false;
  }

  @Override
  public SafeValue getSafeValue(final Object key) {
    TimeValue value = values.get(key);
    if (value == null) {
      return null;
    }

    return new CompareAndSwapSafeValue(value.value);
  }

  @Override
  public Long increment(Object o, Long l) {
    if (!values.containsKey(o)) {
      values.put(o, new TimeValue(0, l));
      return l;
    }

    Integer expTime = values.get(o).seconds;

    if (values.get(o).value instanceof Long) {
      Long oldValue = (Long) values.get(o).value;
      Long newValue = oldValue + l;

      values.put(o, new TimeValue(expTime, newValue));
      return newValue;
    }

    return null;
  }

  @Override
  public boolean contains(Object key) {
    return values.containsKey(key);
  }

  @Override
  public void flushCache() {
    flush();
  }

  public void flush() {
    values = new HashMap<Object, TimeValue>();
  }

  public void hasCachedKey(Object key) {
    assertTrue("expected " + key + " was not stored in cache?", values.containsKey(key));
  }

  public void hasNotCachedKey(Object key) {
    assertFalse("expected " + key + " was stored in cache where it shouldn't ?", values.containsKey(key));
  }

  public void hasCachedValue(Object value) {
    for (TimeValue each : values.values()) {
      if (each.value.equals(value)) {
        return;
      }
    }
    fail(value + "was not being cached");
  }
}
