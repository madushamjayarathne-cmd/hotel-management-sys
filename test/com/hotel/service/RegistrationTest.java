package com.hotel.service;

import com.hotel.facade.HotelFacade;
import com.hotel.service.checkout.*;
import com.hotel.service.registration.*;
import com.hotel.testing.Check;

import java.math.BigDecimal;

public class RegistrationTest {
  public static void testFirstGuestGetsFirstFreeRoom() {
    var h = new TestHotel();
    var r = h.register("John", "Smith");
    Check.equal(1, r.room().number(), "room");
    Check.equal("John Smith", r.stay().guest().toString(), "guest");
    Check.equal(new BigDecimal("20.00"), r.stay().dailyPrice(), "daily price stored");
    Check.isTrue(r.stay().active(), "active");
  }

  public static void testRoomsFilledInOrderAcrossClasses() {
    var h = new TestHotel();
    String[] names = {"Aaa", "Bbb", "Ccc", "Ddd", "Eee", "Fff"};
    for (int i = 0; i < 6; i++) {
      var r = h.register(names[i], "Test");
      Check.equal(i + 1, r.room().number(), "room " + (i + 1));
      Check.equal(i < 3 ? "STANDARD" : "BUSINESS", r.room().type(), "class of room " + (i + 1));
    }
  }

  public static void testNoFreeRooms() {
    var h = new TestHotel();
    String[] names = {"Aaa", "Bbb", "Ccc", "Ddd", "Eee", "Fff"};
    for (String n : names) {
      h.register(n, "Test");
    }
    Check.isTrue(
        h.service.register("Ggg", "Test") instanceof RegistrationResult.NoFreeRooms, "full hotel");
  }

  public static void testFreedRoomIsReused() {
    var h = new TestHotel();
    h.register("Aaa", "Test");
    h.register("Bbb", "Test");
    h.checkOut(1);
    Check.equal(1, h.register("Ccc", "Test").room().number(), "room 1 reused");
  }

  public static void testInvalidGuestRejected() {
    var h = new TestHotel();
    for (String[] bad :
        new String[][] {
          {"", "Smith"}, {"John", ""}, {"J0hn", "Smith"}, {"John", "Sm!th"}, {null, null}
        }) {
      Check.isTrue(
          h.service.register(bad[0], bad[1]) instanceof RegistrationResult.InvalidGuest,
          "should be invalid: " + bad[0] + "/" + bad[1]);
    }
    Check.equal(0, h.service.occupiedRooms().size(), "nothing registered");
  }

  public static void testDuplicateGuestRejectedWhileCheckedIn() {
    var h = new TestHotel();
    h.register("John", "Smith");
    var result = h.service.register("jOHN", "smith");
    if (result instanceof RegistrationResult.AlreadyRegistered(var existing)) {
      Check.equal(1, existing.roomNumber(), "existing room");
    } else {
      throw new AssertionError("expected AlreadyRegistered but was " + result);
    }
    Check.equal(1, h.service.occupiedRooms().size(), "no second room taken");
  }

  public static void testSameGuestCanReturnAfterCheckOut() {
    var h = new TestHotel();
    h.register("John", "Smith");
    h.checkOut(1);
    Check.isTrue(
        h.service.register("John", "Smith") instanceof RegistrationResult.Registered,
        "returning guest");
  }

  public static void testDifferentGuestsWithSameFirstNameAllowed() {
    var h = new TestHotel();
    h.register("John", "Smith");
    Check.isTrue(
        h.service.register("John", "Brown") instanceof RegistrationResult.Registered,
        "different surname");
  }
}
