package com.clouway.api.pcache.testing;

import com.clouway.api.pcache.CacheException;
import com.clouway.api.pcache.CacheManager;
import com.clouway.api.pcache.CacheTime;
import com.clouway.api.pcache.MatchResult;
import com.clouway.api.pcache.SafeValue;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
public abstract class CacheManagerContract {

  static class NotSerializableObject {

  }

  @Rule
  public JUnitRuleMockery context = new JUnitRuleMockery();

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
    assertEquals(result, value);
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

    assertNull(cacheManager.get(key));

  }

  @Test
  public void safeUpdateValue() {
    cacheManager.put("key1", "value 1");

    SafeValue user1Get = cacheManager.getSafeValue("key1");
    SafeValue user2Get = cacheManager.getSafeValue("key1");

    assertTrue(cacheManager.safePut("key1", user2Get, "user 2 change"));
    assertFalse(cacheManager.safePut("key1", user1Get, "user 1 change"));

    assertThat((String) cacheManager.get("key1"), is("user 2 change"));
  }

  @Test
  public void orderOfSafeGetDoesNotMatter() {
    cacheManager.put("key1", "value 1");

    SafeValue user1Get = cacheManager.getSafeValue("key1");
    SafeValue user2Get = cacheManager.getSafeValue("key1");

    assertTrue(cacheManager.safePut("key1", user1Get, "user 1 change"));
    assertFalse(cacheManager.safePut("key1", user2Get, "user 2 change"));

    assertThat((String) cacheManager.getSafeValue("key1").getValue(), is("user 1 change"));
  }

  @Test
  public void safeUpdateValueForCertainPeriod() throws Exception {

    cacheManager.put("key1", "value 1");

    SafeValue safeValue = cacheManager.getSafeValue("key1");

    assertTrue(cacheManager.safePut("key1", safeValue, "value 2", 10));
    assertThat((String) cacheManager.getSafeValue("key1").getValue(), is("value 2"));
  }

  @Test
  public void nullSafeValueIsReturnedWhenUnknownKeyIsRequested() {
    SafeValue expectingNullValueToBeReturned = cacheManager.getSafeValue("unknown key");
    assertNull(expectingNullValueToBeReturned);
  }

  @Test
  public void flushAllCache() {
    cacheManager.put("::key1::", "::value1::");
    cacheManager.put("::key2::", "::value2::");

    cacheManager.flushCache();

    Object value1 = cacheManager.get("::key1::");
    Object value2 = cacheManager.get("::key2::");

    assertNull(value1);
    assertNull(value2);
  }

  @Test
  public void flushCacheWhenThereIsNoItems() {
    cacheManager.flushCache();
  }

  @Test
  public void getManyValuesAtOnce() {
    cacheManager.put("::key1::", "::value1::");
    cacheManager.put("::key2::", "::value2::");

    MatchResult<String> result = cacheManager.getAll( Arrays.asList("::key1::", "::key2::"), String.class);

    assertThat(result.hasMissedKeys(), is(equalTo(false)));
    assertThat(result.getHits(), containsInAnyOrder("::value1::", "::value2::"));
  }

  @Test
  public void partialCacheHit() {
    cacheManager.put("::key1::", "::value1::");

    MatchResult<String> result = cacheManager.getAll(Arrays.asList("::key1::", "::key2::"), String.class);

    assertThat(result.getHits().size(), is(equalTo(1)));
    assertThat(result.hasMissedKeys(), is(equalTo(true)));
    assertThat(result.getHits(), containsInAnyOrder("::value1::"));
    assertThat(result.getMissedKeys(), containsInAnyOrder("::key2::"));
  }

  @Test
  public void noCacheHits() {
    MatchResult<String> result = cacheManager.getAll(Arrays.asList("::key1::", "::key2::"), String.class);

    assertThat(result.getHits().size(), is(equalTo(0)));
    assertThat(result.getMissedKeys(), containsInAnyOrder("::key1::", "::key2::"));
  }

  @Test
  public void getPartialCacheHitsWithUsingPrefix() {
    cacheManager.put("prefix::key1::", "::value1::");

    MatchResult<String> result = cacheManager.getAll("prefix", Arrays.asList("::key1::", "::key2::"), String.class);

    assertThat(result.getHits().size(), is(equalTo(1)));
    assertThat(result.hasMissedKeys(), is(equalTo(true)));
    assertThat(result.getHits(), containsInAnyOrder("::value1::"));
    assertThat(result.getMissedKeys(), containsInAnyOrder("::key2::"));
  }

  @Test
  public void tryToGetObjectFromDifferentType() {
    cacheManager.put("::key1::", 540000000000000000L);

    MatchResult<Character> booleanResult = cacheManager.getAll( Arrays.asList("::key1::"), Character.class);

    assertThat(booleanResult.hasMissedKeys(), is(equalTo(true)));
  }

  @Test
  public void tryToGetSomeObjectsFromDifferentType() {
    cacheManager.put("::key1::", "::value::");
    cacheManager.put("::key2::", true);

    MatchResult<String> stringResult = cacheManager.getAll("", Arrays.asList("::key1::", "::key2::"), String.class);
    MatchResult<Boolean> booleanResult = cacheManager.getAll("", Arrays.asList("::key1::", "::key2::"), Boolean.class);

    assertThat(stringResult.getMissedKeys(), is(equalTo(Arrays.asList("::key2::"))));
    assertThat(stringResult.getHits(), is(equalTo(Arrays.asList("::value::"))));

    assertThat(booleanResult.getMissedKeys(), is(equalTo(Arrays.asList("::key1::"))));
    assertThat(booleanResult.getHits(), is(equalTo(Arrays.asList(true))));
  }

  @Test
  public void getCompositeObject() {
    Person person = new Person("Stanimir", 20);

    cacheManager.put("::key1::", person);
    cacheManager.put("::key2::", 15);

    MatchResult<Person> result = cacheManager.getAll("", Arrays.asList("::key1::", "::key2::"), Person.class);

    assertThat(result.getMissedKeys(), is(equalTo(Arrays.asList("::key2::"))));
    assertThat(result.getHits().get(0), is(equalTo(person)));
  }

  @Test
  public void lockUnknownKey() {
    assertTrue(cacheManager.lock("::key::"));
    assertFalse(cacheManager.lock("::key::"));
  }

  @Test
  public void lockExistingKey() {
    cacheManager.put("::key::", 1);
    assertFalse(cacheManager.lock("::key::"));
  }

  protected abstract CacheManager createCacheManager();
}

class Person implements Serializable {
  private String name;
  private int age;

  public Person(String name, int age) {
    this.name = name;
    this.age = age;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Person person = (Person) o;
    return age == person.age &&
            Objects.equals(name, person.name);
  }

  @Override
  public int hashCode() {

    return Objects.hash(name, age);
  }
}
