package com.clouway.api.pcache;

/**
 * Represents period of time in seconds
 * @author mlesikov  {mlesikov@gmail.com}
 */
public enum CacheTime {
  HALF_HOUR(30*60),TEN_MINUTES(10*60),ONE_HOUR(60*60), EIGHT_HOURS(60 * 60 * 8),FIVE_MINUTES(5*60), TEN_SECONDS(10),TWO_SECONDS(2),ONE_MINUTE(60),ZERO(0);

  private final Integer seconds;

  CacheTime(Integer seconds) {
    this.seconds = seconds;
  }


  public Integer getSeconds() {
    return seconds;
  }

  public static CacheTime seconds(Integer seconds){
    for (CacheTime t : CacheTime.values()) {
        if (t.getSeconds().equals(seconds)) {
          return t;
        }
      }
      return null;
  }
}
