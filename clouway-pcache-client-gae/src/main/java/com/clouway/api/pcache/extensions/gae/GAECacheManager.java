package com.clouway.api.pcache.extensions.gae;

import com.clouway.api.pcache.CacheExceptionTranslator;
import com.clouway.api.pcache.CacheManager;
import com.clouway.api.pcache.MissedHitsProvider;
import com.clouway.api.pcache.MatchResult;
import com.clouway.api.pcache.SafeValue;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheService.IdentifiableValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author mlesikov  {mlesikov@gmail.com}
 */
 class GAECacheManager implements CacheManager {
  private Logger log = Logger.getLogger(GAECacheManager.class.getName());
  private MemcacheService memcacheService;
  private CacheExceptionTranslator translator;

  public GAECacheManager(MemcacheService memcacheService, CacheExceptionTranslator translator) {
    this.memcacheService = memcacheService;
    this.translator = translator;
  }

  /**
   * Puts an Object in the cache with a specified key for a specified period of time(in seconds)
   *
   * @param key     the key of the object
   * @param value   the object
   * @param seconds the length of the period in seconds
   */
  public void put(String key, Object value, Integer seconds) {
    put(key, value, seconds * 1000l);
  }

  /**
   * Puts an Object in the cache with a specified key for a specified period of time(in seconds)
   *
   * @param key   the key of the object
   * @param value the object
   * @param mills the length of the period in mills
   */
  public void put(String key, Object value, Long mills) {
    try {
      memcacheService.put(key, value, Expiration.byDeltaMillis(mills.intValue()));
    } catch (Exception e) {
      translator.translate(e);
    }
    log.info(value.getClass().getSimpleName() + "  with key :" + key + " was STORED in GAE CACHE for " + mills + " mills !");
  }

  public void put(String key, Object value) {
    memcacheService.put(key, value);
  }


  /**
   * Gets an object from the cache by it's key
   *
   * @param key the key
   * @return the object if it exists with the specified key in the cache
   */
  public Object get(String key) {
    Object result;
    try {
      result = memcacheService.get(key);
    } catch (Exception e) {
      return null;
    } if(result!=null){
      log.info(result.getClass().getSimpleName()+"  with key :"+key+" was RETURNED FROM GAE CACHE !");
    }
    return result;
  }

    @Override
    @SuppressWarnings("unchecked")
    public <V> MatchResult<V> getAll(List<String> keys, Class<V> clazz) {
      Map<String, V> hits = new HashMap<>();
      List<String> missed = new LinkedList<>();

      try {
        Map<String, Object> rawHits = memcacheService.getAll(keys);
        for(String key : keys) {
            if(!rawHits.containsKey(key)) {
                missed.add(key);
            } else {
                try {
                    hits.put(key, clazz.cast(rawHits.get(key)));
                } catch (ClassCastException e) {
                    missed.add(key);
                }
            }
        }
      } catch (Exception e) {
        missed = keys;
      }

      return new MatchResult(new ArrayList<>(hits.values()), missed);
    }

    @Override
    public <T> List<T> getAll(List<String> keys, Class<T> clazz, MissedHitsProvider<T> missedHitsProvider) {
        List<T> output = new LinkedList<>();
        MatchResult<T> result = getAll(keys, clazz);

        output.addAll(result.getHits());
        if(result.hasMissedKeys()) output.addAll(missedHitsProvider.get(result.getMissedKeys()));

        return output;
    }

    /**
   *
   * @param key the key
   */
  public void remove(String key) {
    try {
      memcacheService.delete(key);
    } catch (Exception e) {
      translator.translate(e);
    }
    log.info("Object with key :"+key+" was REMOVED FROM GAE CACHE !");
  }

  @Override
  public boolean safePut(Object key, SafeValue safeValue, Object value) {
    GaeSafeValue gaeSafeValue = (GaeSafeValue) safeValue;
    return memcacheService.putIfUntouched(key, gaeSafeValue.getIdentifiableValue(), value);
  }

  @Override
  public boolean safePut(Object key, SafeValue safeValue, Object value, Integer expiration) {
    GaeSafeValue gaeSafeValue = (GaeSafeValue) safeValue;
    return memcacheService.putIfUntouched(key, gaeSafeValue.getIdentifiableValue(), value, Expiration.byDeltaSeconds(expiration));
  }

  @Override
  public SafeValue getSafeValue(Object key) {
    IdentifiableValue result = memcacheService.getIdentifiable(key);
    if (result == null) {
      return null;
    }

    return new GaeSafeValue(result);
  }

  @Override
  public Long increment(Object o, Long l) {
    return memcacheService.increment(o, l);
  }

  @Override
  public boolean contains(Object key) {
    return memcacheService.contains(key);
  }

  @Override
  public void flushCache() {
    memcacheService.clearAll();
    log.info("Cache was flushed for namespace: " + memcacheService.getNamespace());
  }
}
