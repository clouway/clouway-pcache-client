package com.clouway.api.pcache.extensions.redis;

import com.clouway.api.pcache.CacheManager;
import com.clouway.api.pcache.NamespaceProvider;
import com.clouway.api.pcache.extensions.redis.util.RedisStorageClient;
import redis.clients.jedis.Jedis;

/**
 * RedisCacheManagerFactory is a CacheManagerFactory.
 *
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
public final class RedisCacheManagerFactory {

  /**
   * The default redis host used internally in k8s. Note that every client is able to override
   * this host by using the REDIS_HOST environment variable.
   */
  private static final String DEFAULT_REDIS_HOST = "redis-master.db.svc.cluster.local";

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
  public static CacheManager create() {
    String redistHost = System.getenv("REDIS_HOST");
    if (redistHost == null || "".equals(redistHost)) {
      redistHost = DEFAULT_REDIS_HOST;
    }

    return create(redistHost, DEFAULT_NAMESPACE_PROVIDER);
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
   * @param redisHost the host of the Redis server.
   * @return the newly created cache manager
   */
  public static CacheManager create(String redisHost, NamespaceProvider namespaceProvider) {
    Jedis jedis;

    if (redisHost.contains(":")) {
      String[] parts = redisHost.split(":");
      jedis = new Jedis(parts[0], Integer.parseInt(parts[1]));
    } else {
      jedis = new Jedis(redisHost);
    }
    
    return new RedisCacheManager(jedis, new RedisStorageClient("redis://" + redisHost, 30), namespaceProvider);
  }
}
