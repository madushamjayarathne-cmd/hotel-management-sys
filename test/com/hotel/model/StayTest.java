package com.hotel.model;

import com.hotel.testing.Check;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class StayTest {
  private static final Guest GUEST = new Guest("Anna", "Lee");

  private static java.time.Instant at(int day, int hour, int minute) {
    return LocalDateTime.of(2026, 3, day, hour, minute).atZone(ZoneId.systemDefault()).toInstant();
  }

  private static Stay stay(java.time.Instant in) {
    return new Stay(1, 4, GUEST, new BigDecimal("30.00"), in, null);
  }

  public static void testActiveUntilCheckedOut() {
    Stay s = stay(at(10, 12, 0));
    Check.isTrue(s.active(), "new stay is active");
    Stay done = s.checkedOut(at(11, 12, 0));
    Check.isTrue(!done.active(), "checked out stay is not active");
    Check.equal(at(11, 12, 0), done.checkOut(), "check-out time");
    Check.isTrue(s.active(), "original record unchanged");
  }

  public static void testSameDayCountsAsOneDay() {
    Check.equal(1L, stay(at(10, 8, 0)).days(at(10, 20, 0)), "same day");
    Check.equal(1L, stay(at(10, 8, 0)).days(at(10, 8, 0)), "zero length");
  }

  public static void testOverMidnightCountsCalendarDays() {
    Check.equal(1L, stay(at(10, 23, 59)).days(at(11, 0, 1)), "two minutes over midnight is 1 day");
    Check.equal(
        1L, stay(at(10, 8, 0)).days(at(11, 7, 0)), "less than 24h across midnight is 1 day");
    Check.equal(2L, stay(at(10, 23, 59)).days(at(12, 0, 1)), "two midnights is 2 days");
  }

  public static void testMultipleDays() {
    Check.equal(3L, stay(at(10, 12, 0)).days(at(13, 12, 0)), "3 days");
    Check.equal(new BigDecimal("90.00"), stay(at(10, 12, 0)).total(at(13, 12, 0)), "3 x 30.00");
  }

  public static void testTotalUsesStoredDailyPrice() {
    Check.equal(new BigDecimal("30.00"), stay(at(10, 9, 0)).total(at(10, 18, 0)), "same day total");
  }

  public static void testWithIdKeepsOtherFields() {
    Stay s = stay(at(10, 12, 0)).withId(7);
    Check.equal(7L, s.id(), "id");
    Check.equal(4, s.roomNumber(), "room");
    Check.equal(new BigDecimal("30.00"), s.dailyPrice(), "price");
  }

  public static void testDailyPriceRequired() {
    Check.throwsException(
        NullPointerException.class,
        () -> new Stay(1, 1, GUEST, null, at(10, 12, 0), null),
        "null price");
  }
}
