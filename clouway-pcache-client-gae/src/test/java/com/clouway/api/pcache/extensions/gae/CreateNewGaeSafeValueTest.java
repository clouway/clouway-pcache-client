package com.clouway.api.pcache.extensions.gae;

import com.google.appengine.api.memcache.MemcacheService.IdentifiableValue;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Miroslav Genov (mgenov@gmail.com)
 */
public class CreateNewGaeSafeValueTest {

  class AnyIdentifiableValue implements IdentifiableValue {

    private final Object value;

    public AnyIdentifiableValue(Object value) {
      this.value = value;
    }

    @Override
    public Object getValue() {
      return value;
    }
  }

  @Test
  public void happyPath() {
    Object o = new Object();
    AnyIdentifiableValue anyIdentifiableValue = new AnyIdentifiableValue(o);
    GaeSafeValue value = new GaeSafeValue(anyIdentifiableValue);
    assertThat(value.getValue(), is(sameInstance(o)));
  }

  @Test
  public void missingValue() {
    try {
      new GaeSafeValue(null);
      fail("Why can I create GaeSafeValue with no value?");
    } catch (IllegalArgumentException e) {

    }
  }

}
