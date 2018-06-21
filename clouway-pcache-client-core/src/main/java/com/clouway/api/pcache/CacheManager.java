package com.clouway.api.pcache;

import java.util.List;

/**
 * Represents s cache manager which is responsible for all caching operations.
 *
 * @author mlesikov  {mlesikov@gmail.com}
 */
public interface CacheManager {

  /**
    * Puts an Object in the cache with a specified key for a specified period of time(in seconds)
    *
    * @param key       the key of the object
    * @param value     the object
    * @param cacheTimeSeconds the length of the period in seconds
    */
   void put(String key, Object value, Integer cacheTimeSeconds);

  /**
    * Puts an Object in the cache with a specified key for a specified period of time(in seconds)
    *
    * @param key       the key of the object
    * @param value     the object
    * @param mills the length of the period in mills
    */
   void put(String key, Object value, Long mills);

   /**
    * Puts an Object in the cache.
    *
    * @param key   the key on which Object will be mapped
    * @param value the object that will be cached
    */
   void put(String key, Object value);

   /**
    * Gets an object from the cache by it's key
    *
    * @param key the key
    * @return the object if it exists with the specified key in the cache
    */
   Object get(String key);

    /**
     * Gets a list of objects from the cache by their keys
     *
     * @param keys the keys of desired objects
     * @return object representing the work done
     */
   <V> MatchResult<V> getAll(List<String> keys, Class<V> clazz);

    /**
     * Gets a list with cached objects by their keys and get missed from function
     *
     * @param keys the keys of desired objects
     * @param missedHitsProvider implementation of the MissedHitsProvider interface
     * @return list of objects
     */
   <T> List<T> getAll(List<String> keys, Class<T> clazz, MissedHitsProvider<T> missedHitsProvider);

   /**
    * Removes an object from the cache by it's key
    *
    * @param key the key
    */
   void remove(String key);

   /**
    * Safely puts a new value in memcache. By safely is meaning no other modification are being made to the value
    * between {@link CacheManager#getSafeValue(Object)} invocation and {@link CacheManager#safePut(Object, SafeValue, Object)}
    *
    * @param key       the key which value to be updated
    * @param safeValue a reference to the safe value
    * @param value     the new value to be put for the key
    * @return true if value is successfully updated or false in other case
    */
   boolean safePut(Object key, SafeValue safeValue, Object value);

  /**
   * Safely puts a new value in memcache. By safely is meaning no other modification are being made to the value
   * between {@link CacheManager#getSafeValue(Object)} invocation and {@link CacheManager#safePut(Object, SafeValue, Object, Integer)}
   *
   * @param key       the key which value to be updated
   * @param safeValue a reference to the safe value
   * @param value     the new value to be put for the key
   * @param expiration in seconds after it value is not in cache anymore
   * @return true if value is successfully updated or false in other case
   */
   boolean safePut(Object key, SafeValue safeValue, Object value, Integer expiration);

   /**
    * Gets value from cache which is safe value (value that could be updated safely in concurrent manner).
    *
    * @param key the key to which value is bound
    * @return a safe value which need to be
    */
   SafeValue getSafeValue(Object key);

  /**
   * Increment an object value in cache.
   *
   * @param o   the object that value will be updated
   * @param l   the amount to increment with
   * @return the new incremented value
   */
   Long increment(Object o, Long l);

  /**
   * Check the cache contains object with given key
   *
   * @param key the key of object that is gonna be checked
   * @return true if contains that object, false if not
   */
   boolean contains(Object key);

  /**
   * Flush all cache
   */
  void flushCache();
}