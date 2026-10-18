package com.hotel.testing;

import java.time.*;

/** Clock whose time is set and advanced by the test. */
public class MutableClock extends Clock {
  private Instant now;

  public MutableClock(LocalDateTime start) {
    this.now = start.atZone(ZoneId.systemDefault()).toInstant();
  }

  public void set(LocalDateTime time) {
    now = time.atZone(ZoneId.systemDefault()).toInstant();
  }

  public void plusDays(long days) {
    now = now.plus(Duration.ofDays(days));
  }

  @Override
  public ZoneId getZone() {
    return ZoneId.systemDefault();
  }

  @Override
  public Clock withZone(ZoneId zone) {
    return this;
  }

  @Override
  public Instant instant() {
    return now;
  }
}
