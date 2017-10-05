package com.clouway.api.pcache.extensions.gae;

import com.clouway.api.pcache.CacheManager;
import com.clouway.api.pcache.testing.CacheManagerContract;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.Before;

/**
 * @author mlesikov  {mlesikov@gmail.com}
 */
public class GAECacheManagerTest extends CacheManagerContract {


  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
          new LocalMemcacheServiceTestConfig());

  @Before
  public void before() {
    helper.setUp();
  }

  @Override
  protected CacheManager createCacheManager() {
    return new GAECacheManager(MemcacheServiceFactory.getMemcacheService(), new GAECacheExceptionTranslator());
  }
}
