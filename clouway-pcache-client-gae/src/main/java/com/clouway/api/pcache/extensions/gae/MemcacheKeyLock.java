package com.clouway.api.pcache.extensions.gae;

import com.clouway.api.pcache.KeyLock;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheService.SetPolicy;
import com.google.inject.Inject;

/**
 * @author Mihail Lesikov (mlesikov@gmail.com)
 */
class MemcacheKeyLock implements KeyLock {

  private final MemcacheService memcacheService;

  @Inject
  public MemcacheKeyLock(MemcacheService memcacheService) {
    this.memcacheService = memcacheService;
  }

  /**
   * Locks the given key for a specified period of time.
   * If successfully locks the key returns true,
   * if the key was already locked returns false.
   * @param key - the key
   * @param mills - time for locking the key in mills
   * @return true if key not locked and successfully locked, false otherwise
   */
  @Override
  public boolean lock(String key, Long mills) {

    if (mills == null || mills < 0) {
      throw new IllegalArgumentException(" invalid mills value " + mills);
    }
    
    return memcacheService.put(key, 1, Expiration.byDeltaMillis(mills.intValue()), SetPolicy.ADD_ONLY_IF_NOT_PRESENT);
  }
}
