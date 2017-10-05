package com.clouway.api.pcache.extensions.gae;

import com.clouway.api.pcache.Lock;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.inject.Inject;

/**
 * @author Georgi Georgiev (GeorgievJon@gmail.com)
 */
class MemcacheLock implements Lock {

  private final MemcacheService memcacheService;

  @Inject
  public MemcacheLock(MemcacheService memcacheService) {
    this.memcacheService = memcacheService;
  }

  @Override
  public void lock(String key, int retryCount) {
    boolean wait = true;
    int sleepPeriod = 50;
    int defaultExpirationTime = retryCount * sleepPeriod;
    int retry = 0;
    do {
      MemcacheService.IdentifiableValue iv = memcacheService.getIdentifiable(key);
      if (iv == null) {
        memcacheService.put(key, 1, Expiration.byDeltaMillis(defaultExpirationTime));
        iv = memcacheService.getIdentifiable(key);
        wait = !memcacheService.putIfUntouched(key, iv, 2, Expiration.byDeltaMillis(defaultExpirationTime));
      }

      sleep(sleepPeriod);

      if (retry == retryCount) {
        throw new IllegalStateException("Lock timeout.Please be sure that you are using proper timeout or your" +
                "operation does not take much more time.");
      }

      retry++;
    } while (wait && retry <= retryCount);

  }

  @Override
  public void releaseLock(String key) {
    memcacheService.delete(key);
  }

  @Override
  public void releaseLock(String key, long delayMills) {
    try {
      Thread.sleep(delayMills);
    } catch (InterruptedException e) {

      e.printStackTrace();

    } finally {
      releaseLock(key);
    }
  }


  private void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
