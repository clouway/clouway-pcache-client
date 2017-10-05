package com.clouway.api.pcache;

/**
 * @author Mihail Lesikov (mlesikov@gmail.com)
 */
public interface KeyLock {

  /**
   * Locks the given key for a specified period of time.
   * If successfully locks the key returns true,
   * if the key was already locked returns false.
   *
   * @param key   - the key
   * @param mills - time for locking the key in mills
   * @return true if key not locked and successfully locked, false otherwise
   */
  boolean lock(String key, Long mills);

}
