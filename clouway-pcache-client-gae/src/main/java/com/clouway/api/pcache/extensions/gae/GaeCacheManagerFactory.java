package com.clouway.api.pcache.extensions.gae;

import com.clouway.api.pcache.CacheManager;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

/**
 * @author Vasil Mitov (vasil.mitov@clouway.com)
 */
public final class GaeCacheManagerFactory {

  public static CacheManager create() {
    MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();
    memcacheService.setErrorHandler(ErrorHandlers.getDefault());
    return new GAECacheManager(memcacheService, new GAECacheExceptionTranslator());
  }
}

