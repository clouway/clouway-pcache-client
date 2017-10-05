package com.clouway.api.pcache.extensions.gae;

import com.clouway.api.pcache.KeyLock;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Mihail Lesikov (mlesikov@gmail.com)
 */
public class MemcacheKeyLockTest {


  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
          new LocalMemcacheServiceTestConfig());


  private KeyLock keyLock;
  private MemcacheService memcacheService;

  @Before
  public void setUp() throws Exception {
    helper.setUp();
    memcacheService = MemcacheServiceFactory.getMemcacheService();
    keyLock = new MemcacheKeyLock(memcacheService);
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void lockKey() throws Exception {
    assertTrue(keyLock.lock("key", 1000L));
  }

  @Test
  public void lockKeyUnsuccessfulWhenAlreadyLockedKey() throws Exception {
    assertTrue(keyLock.lock("key1", 5000L));
    assertFalse(keyLock.lock("key1", 50L));
  }

  @Test
  public void lockKeySuccessfulAfterLockPeriod() throws Exception {
    assertTrue(keyLock.lock("key2", 1000L));
    memcacheService.delete("key2");
    assertTrue(keyLock.lock("key2", 1000L));
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidMills() throws Exception {
    assertTrue(keyLock.lock("key3", null));
  }

  @Test(expected = IllegalArgumentException.class)
  public void lessThanZeroMills() throws Exception {
    assertTrue(keyLock.lock("key4", -1L));
  }

}
