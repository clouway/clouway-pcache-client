package com.clouway.api.pcache.extensions.gae;

import com.google.inject.AbstractModule;
import com.google.inject.Module;


/**
 * Represents a providedservice used for providing cache  providedservice
 *
 * @author mlesikov  {mlesikov@gmail.com}
 */
public final class CacheService {
  private AbstractModule module;

  private CacheService(AbstractModule module) {
    this.module = module;
  }

  /**
   * Creates a new Cache providedservice that using GAE memcache as a target cache provider.
   *
   * @return a newly created cache providedservice.
   */
  public static CacheService usingGAE() {
    return new CacheService(new AbstractModule() {
      @Override
      protected void configure() {
        install(new InternalCacheModule());
        install(new GAECacheModule());
      }
    });
  }

  /**
   * Sets a new providedservice with module specified to  configurate the cache for GAE( Google App Engine)
   *
   * @return a new CacheService inctance
   */
  public final Module buildModule() {
    return module;
  }

}
