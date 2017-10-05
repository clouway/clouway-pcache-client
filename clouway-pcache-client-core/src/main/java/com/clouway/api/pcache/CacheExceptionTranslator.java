package com.clouway.api.pcache;


/**
 * @author mlesikov  {mlesikov@gmail.com}
 */
public interface CacheExceptionTranslator{

  void translate(Exception e) throws CacheException;

}
