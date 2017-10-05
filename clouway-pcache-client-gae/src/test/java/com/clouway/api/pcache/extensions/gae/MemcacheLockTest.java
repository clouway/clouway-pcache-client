package com.clouway.api.pcache.extensions.gae;

import com.clouway.api.pcache.Lock;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * @author Georgi Georgiev (GeorgievJon@gmail.com)
 */
public class MemcacheLockTest {

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
           new LocalMemcacheServiceTestConfig());

  private MemcacheService memcacheService;

  private Lock lock;

  @Before
  public void setUp() throws Exception {
    helper.setUp();
    memcacheService = MemcacheServiceFactory.getMemcacheService();
    lock = new MemcacheLock(memcacheService);
  }

  @Test
  public void lockAndUnlock() {

    lock.lock("test", 50);

    Integer count = (Integer) memcacheService.get("test");
    assertThat("memcache was not properly updated", count, is(2));

    lock.releaseLock("test");
    assertNull("lock is still gained after releaseLock was being invoked", memcacheService.get("test"));
  }

  @Test(expected = IllegalStateException.class)
  public void lockRetryFailsWhenCountReached() {
    // already one has a lock
    memcacheService.put("lock_test", 1l);

    // when we ask for lock
    lock.lock("lock_test", 2);
  }
}
