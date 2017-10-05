package com.clouway.api.pcache.extensions.gae;

import com.clouway.api.pcache.CacheException;
import com.clouway.api.pcache.CacheExceptionTranslator;

/**
 * Represents a exception translator. Transform the received exception from GAE memcache.
 * @author mlesikov  {mlesikov@gmail.com}
 */
 class GAECacheExceptionTranslator implements CacheExceptionTranslator {
  public static final String ILLEGAL_ARGUMENT_EXCEPTION_MESSAGE = "(did you implement Serializable interface)";

  /**
   * Default constructor
   */
  public GAECacheExceptionTranslator() {
  }

  /**
   * Translates the GAE Exceptions that are received.
   * @param exception the received exception
   */
  public void translate(Exception exception) {
    if(exception instanceof IllegalArgumentException){
      throw new CacheException(exception.getMessage()+" "+ILLEGAL_ARGUMENT_EXCEPTION_MESSAGE);
    }else{
        throw new CacheException(exception.getMessage());
    }

  }
}
