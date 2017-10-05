package com.clouway.api.pcache;

/**
 * SafeValue is representing a value object that is safe for concurrent updates.
 *
 * @author Miroslav Genov (mgenov@gmail.com)
 */
public interface SafeValue {

  /**
   * Gets the value that is returned
   *
   * @return the value that is returned
   */
  Object getValue();

}
