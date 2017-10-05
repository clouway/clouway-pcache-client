package com.clouway.api.pcache.testing;

import com.clouway.api.pcache.CacheManager;

/**
 * @author Miroslav Genov (mgenov@gmail.com)
 */
public class InMemoryCacheManagerTest extends CacheManagerContract {

  @Override
  protected CacheManager createCacheManager() {
    return new InMemoryCacheManager();
  }
}
