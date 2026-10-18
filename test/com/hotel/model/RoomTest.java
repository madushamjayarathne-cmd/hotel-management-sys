package com.hotel.model;

import com.hotel.testing.Check;

import java.math.BigDecimal;

public class RoomTest {
  private static final BigDecimal PRICE = new BigDecimal("20.00");

  public static void testStandardRoom() {
    Room r = new StandardRoom(1, PRICE);
    Check.equal(1, r.number(), "number");
    Check.equal(PRICE, r.price(), "price");
    Check.equal("STANDARD", r.type(), "type");
  }

  public static void testBusinessRoomIsFiftyPercentHigher() {
    Room r = new BusinessRoom(4, PRICE, new BigDecimal("1.5"));
    Check.equal(new BigDecimal("30.00"), r.price(), "price");
    Check.equal("BUSINESS", r.type(), "type");
    Check.isTrue(r instanceof StandardRoom, "business room is built by inheritance from standard");
  }

  public static void testBusinessPriceRounded() {
    Room r = new BusinessRoom(4, new BigDecimal("19.99"), new BigDecimal("1.5"));
    Check.equal(new BigDecimal("29.99"), r.price(), "19.99 x 1.5 = 29.985 rounds half up");
  }

  public static void testPriceFollowsConfiguration() {
    Room r = new BusinessRoom(4, new BigDecimal("100.00"), new BigDecimal("1.25"));
    Check.equal(new BigDecimal("125.00"), r.price(), "custom price and multiplier");
  }
}
