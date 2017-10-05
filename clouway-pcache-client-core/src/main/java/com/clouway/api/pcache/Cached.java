package com.clouway.api.pcache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used for mark results of the marked method for caching.
 *
 * Example:
 * <code>
 * {@literal @}Cached(cacheTime = CacheTime.TEN_MINUTES, key = "cities_:cities")
 * List&lt;City&gt; queryForCity({@literal @}Named("cities") final String queryString) {...}
 * </code>
 *
 * @author Mihail Lesikov  (mihail.lesikov@clouway.com)
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cached {

  int cacheTimeSeconds() default 0;

  CacheTime cacheTime() default CacheTime.ZERO;

  String key() default "";

}
