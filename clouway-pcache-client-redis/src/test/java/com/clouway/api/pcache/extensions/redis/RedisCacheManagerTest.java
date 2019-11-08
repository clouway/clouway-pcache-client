package com.clouway.api.pcache.extensions.redis;

import com.clouway.api.pcache.CacheManager;
import com.clouway.api.pcache.NamespaceProvider;
import com.clouway.api.pcache.testing.CacheManagerContract;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

import java.util.LinkedList;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
public class RedisCacheManagerTest extends CacheManagerContract {

  @ClassRule
  public static GenericContainer redis = new GenericContainer<>("redis:5.0.3-alpine")
          .withExposedPorts(6379);
  
  @Test
  public void keysAreNamespaceAware() throws Exception {
    final LinkedList<String> namespaces = new LinkedList<String>();
    namespaces.add("ns1"); //put in ns1
    namespaces.add("ns2"); //put in ns2
    namespaces.add("ns1"); //get from ns1
    namespaces.add("ns2"); //get from ns2

    CacheManager cacheManager = RedisCacheManagerFactory.create(redis.getContainerIpAddress() + ":" + redis.getFirstMappedPort(), new NamespaceProvider() {
      @Override
      public String get() {
        return namespaces.pop();
      }
    });

    cacheManager.put("key1", "value-ns1");
    cacheManager.put("key1", "value-ns2");

    assertThat((String) cacheManager.get("key1"), is(equalTo("value-ns1")));
    assertThat((String) cacheManager.get("key1"), is(equalTo("value-ns2")));
  }

  @Before
  public void setUp() {
    createCacheManager().flushCache();
  }

  @Override
  protected CacheManager createCacheManager() {
    String address = redis.getContainerIpAddress();
    Integer port = redis.getFirstMappedPort();

    return RedisCacheManagerFactory.create(address + ":" + port);
  }
}