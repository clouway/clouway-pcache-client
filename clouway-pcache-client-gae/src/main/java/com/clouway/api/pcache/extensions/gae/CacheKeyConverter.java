package com.clouway.api.pcache.extensions.gae;

import com.clouway.api.pcache.CacheException;
import com.google.inject.name.Named;

import java.lang.annotation.Annotation;

/**
 * Represents a key string converter
 *
 * @author mlesikov  {mlesikov@gmail.com}
 */
public class CacheKeyConverter {
  private static final String SEPARATOR_POINTER = ":";
  public static final String WRONG_ANNOTATION_PARAMETERS = "Wrong parameters were given on the @Cached as key  parameter;";
  public static final String WRONG_METHOD_PARAMETERS = "1. Not all of method's parameters have @Named;" +
          "2. The @Cached key not contains some of the @Named values"+
          "3. Your Key may contains ':' ";

  /**
   * Default constructor
   */
  public CacheKeyConverter() {
  }


  /**
   * Generates a key by the template given form the @Cached annotation an the parameters specified with @Named annotations for the arguments value
   *
   * @param key         the key template
   * @param annotations the method parameters annotations
   * @param args        the method parameters
   * @return the prepared key from the template and the parameter data
   */
  public String generateKeyByNamedAnnotations(String key, Annotation[][] annotations, Object[] args) {
    Named named;
    for (int i = 0; i < annotations.length; i++) {

      if (annotations[i] != null && annotations[i].length > 0 && args.length == annotations.length) {

        named = (Named) annotations[i][0];

        if (key.contains(SEPARATOR_POINTER + named.value())) {

          key = key.replace(SEPARATOR_POINTER + named.value(), args[i].toString());

        } else {
          throw new CacheException(WRONG_ANNOTATION_PARAMETERS);
        }
      } else {
        throw new CacheException(WRONG_METHOD_PARAMETERS);
      }
    }
    return key;
  }


  /**
   * Returns a String constructed of hashcode of the key and the hashcode of every single argumen
   *
   * @param keyAsHashCode the hashcode of the key
   * @param args          arguments
   * @return converted string
   */
  public String getHashCodesAsKey(String keyAsHashCode, Object[] args) {
    if (args != null) {
      for (Object arg : args) {
        if (arg != null) {
          keyAsHashCode = keyAsHashCode + "" + arg.hashCode();
        }
      }
    }
    return keyAsHashCode;
  }

  public boolean isContainsSpecialSymbols(String key) {
    if (key.contains(SEPARATOR_POINTER)) {
      return true;
    }
    return false;
  }
}