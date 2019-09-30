package com.clouway.api.pcache.extensions.redis;

import com.clouway.api.pcache.CacheException;
import com.clouway.api.pcache.CacheManager;
import com.clouway.api.pcache.MatchResult;
import com.clouway.api.pcache.NamespaceProvider;
import com.clouway.api.pcache.SafeValue;
import com.clouway.api.pcache.extensions.redis.RedisFormat.ValueAndFlags;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.params.SetParams;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
class RedisCacheManager implements CacheManager {
  private static final int DEFAULT_TIMEOUT_SECONDS = 3000;
  private final Jedis jedis;
  private final NamespaceProvider namespaceProvider;

  RedisCacheManager(Jedis jedis, NamespaceProvider namespaceProvider) {
    this.jedis = jedis;
    this.namespaceProvider = namespaceProvider;
  }

  @Override
  public void put(String key, Object value, Integer cacheTimeSeconds) {
    try {
      ValueAndFlags valueAndFlag = RedisFormat.format(value);
      byte[] persistentKey = keyOf(key);
      byte[] item = new CacheItem(valueAndFlag.value, valueAndFlag.flags).toByteArray();
      jedis.setex(persistentKey, cacheTimeSeconds, item);
    } catch (IllegalArgumentException ex) {
      throw new CacheException("The received value cannot be serialized.");
    }
  }

  @Override
  public void put(String key, Object value, Long mills) {
    long timeoutInSeconds = mills / 1000;
    put(key, value, (int) timeoutInSeconds);
  }

  @Override
  public void put(String key, Object value) {
    put(key, value, DEFAULT_TIMEOUT_SECONDS);
  }

  @Override
  public void putAll(Map<String, Object> values, Long mills) {
    List<byte[]> keyvalues = new ArrayList<>(values.size() * 2);
    for (Entry<String, Object> entry : values.entrySet()) {
      ValueAndFlags valueAndFlag = RedisFormat.format(entry.getValue());
      byte[] persistentKey = keyOf(entry.getKey());
      byte[] item = new CacheItem(valueAndFlag.value, valueAndFlag.flags).toByteArray();
      keyvalues.add(persistentKey);
      keyvalues.add(item);
    }

    jedis.mset(keyvalues.toArray(new byte[][]{}));
  }

  @Override
  public Object get(String key) {
    byte[] value = jedis.get(keyOf(key));

    if (value == null) {
      return null;
    }

    CacheItem item = CacheItem.parseFrom(value);
    return RedisFormat.parse(item.getValue(), item.getFlags());
  }

  @Override
  public <V> MatchResult<V> getAll(String prefix, List<String> keys, Class<V> clazz) {
    List<V> hits = new LinkedList<>();
    List<String> missed = new LinkedList<>();

    try {
      List<byte[]> prefixedKeys = withPrefix(prefix, keys);
      List<byte[]> rawHits = jedis.mget(prefixedKeys.toArray(new byte[0][]));

      for (int i = 0; i < rawHits.size(); i++) {
        if (rawHits.get(i) == null) {
          missed.add(keys.get(i));
          continue;
        }

        byte[] itemValue = rawHits.get(i);
        CacheItem item = CacheItem.parseFrom(itemValue);

        if (item.getValue() == null) {
          missed.add(keys.get(i));
          continue;
        }
        Object value = RedisFormat.parse(item.getValue(), item.getFlags());

        if (clazz.isInstance(value)) {
          hits.add((V) value);
        } else {
          missed.add(keys.get(i));
        }
      }
    } catch (Exception e) {
      missed = keys;
    }

    return new MatchResult<V>(new ArrayList<V>(hits), missed);

  }

  @Override
  public <V> MatchResult<V> getAll(List<String> keys, Class<V> clazz) {
    return getAll("", keys, clazz);
  }

  @Override
  public void remove(String key) {
    jedis.del(keyOf(key));
  }

  @Override
  public boolean safePut(Object key, SafeValue sv, Object value) {
    return safePut(key, sv, value, 90000);
  }

  @Override
  public boolean safePut(Object key, SafeValue sv, Object value, Integer expiration) {
    if (!(sv instanceof RedisSafeValue)) {
      return false;
    }
    RedisSafeValue safeValue = (RedisSafeValue) sv;

    ValueAndFlags valueAndFlags = RedisFormat.format(value);
    byte[] formattedKey = keyOf(key.toString());

    jedis.watch(formattedKey);
    byte[] targetValue = jedis.get(formattedKey);

    if (targetValue == null) {
      jedis.setex(formattedKey, expiration, valueAndFlags.value);
      jedis.unwatch();
      return false;
    }

    CacheItem item = CacheItem.parseFrom(targetValue);

    ValueAndFlags oldValue = RedisFormat.format(safeValue.getValue());
    Object existingValue = RedisFormat.parse(item.getValue(), item.getFlags());

    ValueAndFlags existingValueAndFlags = RedisFormat.format(existingValue);

    // The existing value was updated by someone else.
    if (!Arrays.equals(existingValueAndFlags.value, oldValue.value)) {
      jedis.unwatch();
      return false;
    }

    Transaction transaction = jedis.multi();
    CacheItem newItem = new CacheItem(valueAndFlags.value, valueAndFlags.flags);
    transaction.set(formattedKey, newItem.toByteArray(), SetParams.setParams().ex(expiration));
    transaction.exec();

    return true;
  }

  @Override
  public boolean lock(String key) {
    SafeValue safeValue = getSafeValue(key);

    if (safeValue == null) {
      put(key, 1);// value does not matter here
      safeValue = getSafeValue(key);

      return safePut(key, safeValue, 2);// value does not matter here
    }

    return false;
  }

  @Override
  public boolean lock(String key, int expiration) {
    SafeValue safeValue = getSafeValue(key);

    if (safeValue == null) {
      put(key, 1, expiration);// value does not matter here
      safeValue = getSafeValue(key);

      return safePut(key, safeValue, 2, expiration);// value does not matter here
    }

    return false;

  }

  @Override
  public SafeValue getSafeValue(Object key) {
    byte[] safeKey = keyOf(key.toString());

    byte[] value = jedis.get(safeKey);
    if (value == null) {
      return null;
    }

    CacheItem item = CacheItem.parseFrom(value);
    if (item != null && item.getValue() == null) {
      return null;
    }

    Object itemValue = RedisFormat.parse(item.getValue(), item.getFlags());
    return new RedisSafeValue(itemValue, 0L);
  }

  @Override
  public Long increment(Object o, Long l) {
    return jedis.incrBy(keyOf(o.toString()), l);
  }

  @Override
  public boolean contains(Object key) {
    return jedis.get(keyOf(key.toString())) != null;
  }

  @Override
  public void flushCache() {
    jedis.flushAll();
  }

  private byte[] keyOf(String key) {
    return String.format("%s:%s", namespaceProvider.get(), key).getBytes(StandardCharsets.UTF_8);
  }

  private List<byte[]> withPrefix(String prefix, List<String> list) {
    List<byte[]> result = new LinkedList<>();

    for (String item : list) {
      result.add(keyOf(prefix + item));
    }

    return result;
  }
}


