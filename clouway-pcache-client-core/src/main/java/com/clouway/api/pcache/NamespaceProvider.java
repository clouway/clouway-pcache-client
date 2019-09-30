package com.clouway.api.pcache;

/**
 * NamespaceProvider is retrieving the name of the namespace to be able to separate properly the cached items.
 * 
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
public interface NamespaceProvider {
  /**
   * Gets the name of the current namespace.
   * 
   * @return the name of the current namespace
   */
  String get();
}
