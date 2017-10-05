package com.clouway.api.pcache.extensions.gae;

import com.clouway.api.pcache.CacheManager;
import com.clouway.api.pcache.CacheTime;
import com.clouway.api.pcache.Cached;
import com.google.inject.Inject;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * CacheInterceptor is an interceptor for adding cache support to all methods.
 * <p/>
 * Sets the period(in seconds) for storing the objects in the cache.Default value is 60seconds(one minute)
 * <p/>
 * <p/>
 * <p/>
 * Sets the key for the object that will be store in the objects in the cache. If it is not set it will be
 * <p/>
 * <p/>
 * generate.
 * <p/>
 *
 * @author mlesikov  {mlesikov@gmail.com}
 */
class CacheInterceptor implements MethodInterceptor {
  private static final CacheTime DEFAULT_CACHE_TIME = CacheTime.ONE_MINUTE;

  private Logger log = Logger.getLogger(CacheInterceptor.class.getName());

  private CacheManager cacheManager;
  private CacheKeyConverter cacheKeyConverter;

  public CacheInterceptor() {

  }

  /**
   * Sets the cacheManager, using GUICE setter injection
   *
   * @param cacheManager the given instance of CacheManager
   */
  @Inject
  public void setCache(CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  /**
   * Sets the cacheKeyConverter, using GUICE setter injection
   *
   * @param cacheKeyConverter the given instance of CacheKeyConverter
   */
  @Inject
  public void setCacheKeyConverter(CacheKeyConverter cacheKeyConverter) {
    this.cacheKeyConverter = cacheKeyConverter;
  }

  /**
   * @param methodInvocation method invocation that will be invoked
   * @return result of the invocation
   * @throws Throwable exception that may be throw in the invocation
   */
  public Object invoke(MethodInvocation methodInvocation) throws Throwable {

    Method method = methodInvocation.getMethod();

    Object[] args = methodInvocation.getArguments();

    Cached cached = methodInvocation.getMethod().getAnnotation(Cached.class);
    Integer cacheTime = getCacheTime(cached);

    Object result;

    String key = cached.key();
    if ("".equals(key)) {
      key = cacheKeyConverter.getHashCodesAsKey("" + method.toGenericString().hashCode(), args);
    } else if (method.getParameterAnnotations().length > 0 && cacheKeyConverter.isContainsSpecialSymbols(key)) {
      key = cacheKeyConverter.generateKeyByNamedAnnotations(key, method.getParameterAnnotations(), args);
    }
    result = cacheManager.get(key);


    if (result == null) {

      log.info("the result of method :" + method.getName() + " was NOT CACHED !");

      result = methodInvocation.proceed();

      // we have to skip null values from methods
      if (result != null) {
        cacheManager.put(key, result, cacheTime);
      }


      log.info("the result of method :" + method.getName() + " was CACHED(or recached) SUCCESSFULLY !");

    }

    return result;
  }

  /**
   * Gets the time for the cache
   *
   * @param cached
   * @return
   */
  private Integer getCacheTime(Cached cached) {
    CacheTime cacheTime = cached.cacheTime();
    if (cacheTime != CacheTime.ZERO) {
       return cacheTime.getSeconds();
    }else if(cached.cacheTimeSeconds()!=0){
      return cached.cacheTimeSeconds();
    }
   return DEFAULT_CACHE_TIME.getSeconds();
  }
}
