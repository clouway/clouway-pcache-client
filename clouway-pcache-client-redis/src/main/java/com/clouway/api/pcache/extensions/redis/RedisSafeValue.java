package com.clouway.api.pcache.extensions.redis;

import com.clouway.api.pcache.SafeValue;

/**
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
class RedisSafeValue implements SafeValue {
  private final Object value;
  private final Long count;

  RedisSafeValue(Object value, Long count) {
    this.value = value;
    this.count = count;
  }

  @Override
  public Object getValue() {
    return value;
  }

  public Long getCount() {
    return count;
  }

}
