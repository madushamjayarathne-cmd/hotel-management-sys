package com.hotel.config;

import com.hotel.testing.Check;

import java.math.BigDecimal;

/** Values come from resources/application.properties and the active environment file. */
public class HotelConfigTest {
  public static void testRoomCountIsPositiveAndEven() {
    int rooms = HotelConfig.getTotalRooms();
    Check.isTrue(rooms > 0 && rooms % 2 == 0, "rooms must be positive and even: " + rooms);
  }

  public static void testPricesAreValid() {
    Check.isTrue(HotelConfig.getStandardPrice().signum() > 0, "standard price must be positive");
    Check.isTrue(
        HotelConfig.getBusinessMultiplier().compareTo(BigDecimal.ONE) > 0,
        "multiplier must be greater than 1");
  }
}
