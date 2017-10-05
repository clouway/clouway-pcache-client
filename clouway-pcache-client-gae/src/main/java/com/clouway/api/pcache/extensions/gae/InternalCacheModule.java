package com.clouway.api.pcache.extensions.gae;

import com.clouway.api.pcache.Cached;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

/**
 * This is a internal module that contains bindings common to all caching
 * modules.
 *
 * @author mlesikov  {mlesikov@gmail.com}
 * @author Miroslav Genov (mgenov@gmail.com)
 */
class InternalCacheModule extends AbstractModule {

  @Override
  protected void configure() {
    CacheInterceptor interceptor = new CacheInterceptor();
    bindInterceptor(Matchers.any(), Matchers.annotatedWith(Cached.class), interceptor);
    requestInjection(interceptor);
  }

  @Override
  public boolean equals(Object o) {
    // Is only ever installed internally, so we don't need to check state.
    return o instanceof InternalCacheModule;
  }

  @Override
  public int hashCode() {
    return InternalCacheModule.class.hashCode();
  }


}
