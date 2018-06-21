package com.clouway.api.pcache.testing;

import com.clouway.api.pcache.CacheException;
import com.clouway.api.pcache.CacheManager;
import com.clouway.api.pcache.CacheTime;
import com.clouway.api.pcache.SafeValue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

/**
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
public abstract class CacheManagerContract {

  static class NotSerializableObject {

  }

  private CacheManager cacheManager;

  @Before
  public void initializeCacheManager() {
    cacheManager = createCacheManager();
  }

  @Test
  public void getWhatWasPut() {
    String key = "kk";
    String value = "val";

    cacheManager.put(key, value);
    String result = (String) cacheManager.get(key);
    Assert.assertEquals(result, value);
  }


  @Test
  public void nonSerializedObject() {
    String key = "key";
    CacheTime cacheTime = CacheTime.TWO_SECONDS;

    try {
      cacheManager.put(key, new NotSerializableObject(), cacheTime.getSeconds());
      Assert.fail("Exception must be thrown");
    } catch (CacheException e) {

    }
  }


  @Test
  public void removedKeyIsNoLongerRetrievable() {
    String key = "key";
    cacheManager.put(key, "some value");

    cacheManager.remove(key);

    Assert.assertNull(cacheManager.get(key));

  }

  @Test
  public void safeUpdateValue() {
    cacheManager.put("key1", "value 1");

    SafeValue user1Get = cacheManager.getSafeValue("key1");
    SafeValue user2Get = cacheManager.getSafeValue("key1");

    Assert.assertTrue(cacheManager.safePut("key1", user2Get, "user 2 change"));
    Assert.assertFalse(cacheManager.safePut("key1", user1Get, "user 1 change"));

    Assert.assertThat((String) cacheManager.get("key1"), is("user 2 change"));
  }

  @Test
  public void orderOfSafeGetDoesNotMatter() {
    cacheManager.put("key1", "value 1");

    SafeValue user1Get = cacheManager.getSafeValue("key1");
    SafeValue user2Get = cacheManager.getSafeValue("key1");

    Assert.assertTrue(cacheManager.safePut("key1", user1Get, "user 1 change"));
    Assert.assertFalse(cacheManager.safePut("key1", user2Get, "user 2 change"));

    Assert.assertThat((String) cacheManager.getSafeValue("key1").getValue(), is("user 1 change"));
  }

  @Test
  public void safeUpdateValueForCertainPeriod() throws Exception {

    cacheManager.put("key1", "value 1");

    SafeValue safeValue = cacheManager.getSafeValue("key1");

    Assert.assertTrue(cacheManager.safePut("key1", safeValue, "value 2", 10));
    Assert.assertThat((String) cacheManager.getSafeValue("key1").getValue(), is("value 2"));
  }

  @Test
  public void nullSafeValueIsReturnedWhenUnknownKeyIsRequested() {
    SafeValue expectingNullValueToBeReturned = cacheManager.getSafeValue("unknown key");
    Assert.assertNull(expectingNullValueToBeReturned);
  }

  @Test
  public void flushAllCache() {
    cacheManager.put("::key1::", "::value1::");
    cacheManager.put("::key2::", "::value2::");

    cacheManager.flushCache();

    Object value1 = cacheManager.get("::key1::");
    Object value2 = cacheManager.get("::key2::");

    Assert.assertNull(value1);
    Assert.assertNull(value2);
  }

  @Test
  public void flushCacheWhenThereIsNoItems() {
    cacheManager.flushCache();
  }

  protected abstract CacheManager createCacheManager();

}
