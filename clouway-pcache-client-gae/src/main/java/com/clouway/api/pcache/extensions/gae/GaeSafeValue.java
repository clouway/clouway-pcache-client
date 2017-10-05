package com.clouway.api.pcache.extensions.gae;

import com.clouway.api.pcache.SafeValue;
import com.google.appengine.api.memcache.MemcacheService.IdentifiableValue;

/**
 * GaeSafeValue is a wrapper class of the GAE's {@link IdentifiableValue}
 * which is used by the {@link GAECacheManager} to wrap it over getSafeValue and safePut methods.
 *
 * @author Miroslav Genov (mgenov@gmail.com)
 */
class GaeSafeValue implements SafeValue {

  private final IdentifiableValue identifiableValue;

  public GaeSafeValue(IdentifiableValue identifiableValue) {
    if (identifiableValue == null) {
      throw new IllegalArgumentException("A GaeSafeVale must have a valid value.");
    }
    this.identifiableValue = identifiableValue;
  }

  @Override
  public Object getValue() {
    return identifiableValue.getValue();
  }

  public IdentifiableValue getIdentifiableValue() {
    return identifiableValue;
  }
}
