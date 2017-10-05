package com.clouway.api.pcache.extensions.gae;

import com.clouway.api.pcache.CacheException;
import com.clouway.api.pcache.CacheTime;
import com.clouway.api.pcache.Cached;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @author mlesikov  {mlesikov@gmail.com}
 */
public class CacheInterceptorTest {

  @Inject
  private GAETestClass test;

  private List<String> value = new ArrayList<String>();

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
          new LocalMemcacheServiceTestConfig()
  );

  @Before
  public void before() {
    helper.setUp();
    value = new ArrayList<String>();
    Guice.createInjector(CacheService.usingGAE().buildModule()).injectMembers(this);
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void getWhatWasPut() {
    value.add("string value one");
    value.add("string value two");
    test.strings = value;
    //first method invoke
    test.getStrings();
    //erasing data
    test.strings = new ArrayList<String>();
    List<String> actual = test.getStrings();
    assertEquals(value, actual);
  }

  @Test
  public void nullResultsFromMethodAreSkipped() {
    assertNull("different result was returned from method that should return null?", test.returnsNull());
  }

  @Test
  public void testPutInCAcheAndGetFromCacheButTheTimeExpired() throws InterruptedException {
    value.add("string value one");
    value.add("string value two");
    test.strings = value;
    //first method invoke
    test.getStrings();
    //erasing data
    test.strings = new ArrayList<String>();
    //waiting for 3 seconds and calling again the test method
    Thread.sleep(3000);

    List<String> actual = test.getStrings();
    assertEquals(test.strings, actual);
  }

  @Test
  public void invokingMethodWithDiferentObjectsAsParametars() {
    value.add("string value one");
    value.add("string value two");
    test.strings = value;
    //first method invoke
    test.getStrings(test, 99);
    List<String> actualResult = test.getStrings(test, 99);
    //erasing data
    test.strings = new ArrayList<String>();
    List<String> actual = test.getStrings(test, 99);
    assertEquals(value, actualResult);
    assertEquals(test.strings, actual);
  }

  @Test
  public void invokingMethodWithDiferentObjectsAsParametarsAndOneOfThemIsNull() {
    value.add("string value one");
    value.add("string value two");
    test.strings = value;
    //first method invoke
    test.getStrings(test, 99);
    List<String> actualResult = test.getStrings(test, 99);
    //erasing data
    test.strings = new ArrayList<String>();
    List<String> actual = test.getStrings(null, 99);
    assertEquals(value, actualResult);
    assertEquals(test.strings, actual);
  }

  @Test
  public void invokingMethodTwoThreeTimesDifferentPeriod() throws InterruptedException {
    value.add("string value five");
    value.add("string value four");
    test.strings = value;
    //first method invoke
    test.getStrings();

    Thread.sleep(1000);
    test.getStrings();

    test.strings = new ArrayList<String>();
    Thread.sleep(2000);

    List<String> actualResult = test.getStrings();
    assertEquals(test.strings, actualResult);
  }

  @Test
  public void invokingMethodWithDiferentParametars() {
    SearchResult expected = test.getResults(3);
    SearchResult actual = test.getResults(3);
    assertEquals(expected, actual);
  }

  @Test
  public void testInvokingMethodWithSPecifiedKey() {
    SearchResult expected = test.getSearchResults(new Double("43.43"));
    SearchResult actual = test.getSearchResults(new Double("55.55"));
    assertEquals(expected, actual);
  }

  @Test
  public void invokingMethodWithSPecifiedKeyThatContainsSpecialSymbol() {
    try {
      test.getSearchResults(new Long("4343"));
    } catch (CacheException e) {
      assertEquals(CacheKeyConverter.WRONG_METHOD_PARAMETERS, e.getMessage());
    }
  }

  @Test
  public void testInvokingMethodWithSPecifiedKeyAndNamedAnotaions() {
    SearchResult expected = test.getResults(2, new Double("43.43"));
    SearchResult actual = test.getResults(2, new Double("55.55"));
    assertFalse(expected.equals(actual));
  }

  @Test
  public void invokingMethodWithSPecifiedKeyAndNamedAnotaionsWrongUsageMethodParams() {
    try {
      test.getString(2, new Double("43.43"));
      fail("exception must be thrown");
    } catch (CacheException e) {
      assertEquals(CacheKeyConverter.WRONG_METHOD_PARAMETERS, e.getMessage());
    }
  }

  @Test
  public void invokingMethodWithSPecifiedKeyAndNamedAnotaionsWrongUsageAnnotationParamsBadKey() {
    try {
      test.getStringTest(2, new Double("43.43"));
      fail("exception must be thrown");
    } catch (CacheException e) {
      assertEquals(CacheKeyConverter.WRONG_ANNOTATION_PARAMETERS, e.getMessage());
    }
  }


  public static class GAETestClass {

    List<String> strings = new ArrayList<String>();

    @Inject
    public GAETestClass() {
    }

    @Cached(cacheTimeSeconds = 2)
    public List<String> getStrings() {
      return strings;
    }

    @Cached(cacheTimeSeconds = 2)
    public String returnsNull() {
      return null;
    }

    @Cached(cacheTime = CacheTime.ONE_MINUTE)
    public List<String> getStrings(GAETestClass gaeTestClass, Integer i) {
      return strings;
    }

    @Cached(cacheTime = CacheTime.TEN_SECONDS)
    public SearchResult<String> getResults(int i) {
      return new SearchResult<String>(i, 2.2d);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      GAETestClass that = (GAETestClass) o;

      if (strings != null ? !strings.equals(that.strings) : that.strings != null) return false;

      return true;
    }

    @Override
    public int hashCode() {
      return strings != null ? strings.hashCode() : 0;
    }

    @Cached(cacheTime = CacheTime.TEN_SECONDS, key = "testkey")
    public SearchResult<String> getSearchResults(Double d) {
      return new SearchResult<String>(4, d);
    }


    @Cached(cacheTime = CacheTime.TEN_SECONDS, key = "testkey _:double_:integer")
    public SearchResult<String> getResults(@Named("integer") Integer i, @Named("double") Double d) {
      return new SearchResult<String>(i, d);
    }

    @Cached(cacheTime = CacheTime.TEN_SECONDS, key = "testkey _:double_:integer")
    public String getString(Integer i, @Named("double") Double d) {
      return "" + i + d;
    }

    @Cached(cacheTime = CacheTime.TEN_SECONDS, key = "testkey _double_:integer")
    public String getStringTest(@Named("integer") Integer i, @Named("double") Double d) {
      return "" + i + d;
    }

    @Cached(cacheTime = CacheTime.TEN_SECONDS, key = "testkey:a")
    public SearchResult getSearchResults(Long d) {
      return new SearchResult(4, new Double(d));
    }
  }

  public static class SearchResult<T> implements Serializable {
    int resultCount = 0;
    Double searchProcessTime = 0.0;

    public SearchResult(int resultCount, Double searchProcessTime) {
      this.resultCount = resultCount;
      this.searchProcessTime = searchProcessTime;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      SearchResult that = (SearchResult) o;

      if (resultCount != that.resultCount) return false;
      if (searchProcessTime != null ? !searchProcessTime.equals(that.searchProcessTime) : that.searchProcessTime != null)
        return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = resultCount;
      result = 31 * result + (searchProcessTime != null ? searchProcessTime.hashCode() : 0);
      return result;
    }
  }
}


