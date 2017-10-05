package com.clouway.api.pcache;

/**
 * Lock is a sample locking functionality which is used to make a boundary between two
 * simultaneous invocations.
 *
 * @author Georgi Georgiev (GeorgievJon@gmail.com)
 */
public interface Lock {

  /**
   * Locks the provided key for specified period of time.
   *
   * @param key        the key to be used for lock
   * @param retryCount the number of retries that need to be performed while lock is being obtained.
   * @throws IllegalStateException is thrown in case lock cannot be gained
   */
  void lock(String key, int retryCount);

  /**
   * Releases last lock.
   *
   * @param key for release
   */
  void releaseLock(String key);

  /**
   * Releases last lock after the specified delay of time in mills
   *
   * @param key        for release
   * @param delayMills delay of time
   */
  void releaseLock(String key, long delayMills);
}
