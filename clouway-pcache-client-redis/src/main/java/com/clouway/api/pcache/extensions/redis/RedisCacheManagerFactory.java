package com.clouway.api.pcache.extensions.redis;

import com.clouway.api.pcache.CacheManager;
import com.clouway.api.pcache.NamespaceProvider;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPooled;

import java.util.HashSet;
import java.util.Set;

/**
 * RedisCacheManagerFactory is a CacheManagerFactory.
 *
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
public final class RedisCacheManagerFactory {

  /**
   * The DEFAULT namespace provider that will be used when none was speicifed by the client code.
   */
  private static final NamespaceProvider DEFAULT_NAMESPACE_PROVIDER = new NamespaceProvider() {
    @Override
    public String get() {
      return "default";
    }
  };

  /**
   * Creates a new instance of {@link CacheManager} that uses Redis.
   *
   * @return the newly created cache manager
   */
  public static CacheManager create(String[] hosts) {
    return create(DEFAULT_NAMESPACE_PROVIDER, hosts);
  }

  /**
   * Creates a new instance of {@link CacheManager} that uses Redis.
   *
   * @param namespaceProvider the namespace provider
   * @return the newly created cache manager
   */
  public static CacheManager create(NamespaceProvider namespaceProvider, String[] hosts) {
    Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
    for (String hostValue : hosts) {
      int port = 6379;
      String host = hostValue;
      if (host.contains(":")) {
        String[] parts = host.split(":");
        host = parts[0];
        port = Integer.parseInt(parts[1]);
      }
      jedisClusterNodes.add(new HostAndPort(host, port));
    }

    JedisCluster jedis = new JedisCluster(jedisClusterNodes);
    return new RedisCacheManager(jedis, namespaceProvider);
  }

  /**
   * Creates a new instance of {@link CacheManager} that uses Redis.
   *
   * @param redisHost the host of the Redis server.
   * @return the newly created cache manager
   */
  public static CacheManager create(String redisHost) {
    return create(redisHost, DEFAULT_NAMESPACE_PROVIDER);
  }


  /**
   * Creates a new instance of {@link CacheManager} that uses Redis.
   *
   * @param redisHost         the host of the Redis server.*
   * @param namespaceProvider the namespace provider used for multi-tenancy
   * @return the newly created cache manager
   */
  public static CacheManager create(String redisHost, NamespaceProvider namespaceProvider) {
    JedisPooled pool;
    if (redisHost.contains(":")) {
      String[] parts = redisHost.split(":");
      pool = new JedisPooled(parts[0], Integer.parseInt(parts[1]));
    } else {
      pool = new JedisPooled(redisHost);
    }
    return new RedisCacheManager(pool, namespaceProvider);
  }
}
