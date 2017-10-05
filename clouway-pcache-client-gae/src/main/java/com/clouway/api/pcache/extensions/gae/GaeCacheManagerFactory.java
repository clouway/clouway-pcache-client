package com.clouway.api.pcache.extensions.gae;

import com.clouway.api.pcache.CacheManager;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

/**
 * @author Vasil Mitov (vasil.mitov@clouway.com)
 */
public final class GaeCacheManagerFactory {

  public static CacheManager create() {
    return new GAECacheManager(MemcacheServiceFactory.getMemcacheService(), new GAECacheExceptionTranslator());
  }
}

