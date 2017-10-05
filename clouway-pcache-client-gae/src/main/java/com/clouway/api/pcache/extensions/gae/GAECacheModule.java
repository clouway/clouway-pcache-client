package com.clouway.api.pcache.extensions.gae;

import com.clouway.api.pcache.CacheManager;
import com.clouway.api.pcache.KeyLock;
import com.clouway.api.pcache.Lock;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * @author mlesikov  {mlesikov@gmail.com}
 */
public class GAECacheModule extends AbstractModule {

  @Override
  protected void configure() {
    CacheManager cacheManager = new GAECacheManager(MemcacheServiceFactory.getMemcacheService(), new GAECacheExceptionTranslator()
    );

    bind(CacheManager.class).toInstance(cacheManager);
  }

  @Provides
  public KeyLock getKeyLock(){
    return new MemcacheKeyLock(MemcacheServiceFactory.getMemcacheService());
  }

  @Provides
  public Lock getLock(){
    return new MemcacheLock(MemcacheServiceFactory.getMemcacheService());
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof GAECacheModule;
  }

  @Override
  public int hashCode() {
    return GAECacheModule.class.hashCode();
  }

}

