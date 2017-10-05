package com.clouway.api.pcache.extensions.gae;

import com.clouway.api.pcache.CacheManager;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @author Miroslav Genov (mgenov@gmail.com)
 */
public class MultiCacheModulesTest {

  @Test
  public void testItShouldAllowCreationOfMultipleCacheModule() {
    Injector injector = Guice.createInjector(CacheService.usingGAE().buildModule(), CacheService.usingGAE().buildModule());
    CacheManager manager = injector.getInstance(CacheManager.class);
    assertNotNull("injected cache manager was null??", manager);
  }

  @Test
  public void testInstallingCacheInDifferentModules() {
    CacheManager manager = Guice.createInjector(new FirstModule(), new SecondModule()).getInstance(CacheManager.class);
    assertNotNull("injected cache manager was null??", manager);
  }

  class FirstModule extends AbstractModule {

    @Override
    protected void configure() {
      install(CacheService.usingGAE().buildModule());
    }

  }

  class SecondModule extends AbstractModule {

    @Override
    protected void configure() {
      install(CacheService.usingGAE().buildModule());
    }

  }
}
