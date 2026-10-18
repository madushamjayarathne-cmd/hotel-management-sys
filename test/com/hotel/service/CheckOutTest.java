package com.hotel.service;

import com.hotel.HotelFactory;
import com.hotel.service.checkout.*;
import com.hotel.testing.Check;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CheckOutTest {
  public static void testSameDayStayCostsOneDay() {
    var h = new TestHotel();
    h.register("John", "Smith");
    h.clock.set(LocalDateTime.of(2026, 3, 10, 20, 0));
    var receipt = h.checkOut(1).receipt();
    Check.equal(1L, receipt.days(), "days");
    Check.equal(new BigDecimal("20.00"), receipt.total(), "total");
  }

  public static void testMultiDayStandardTotal() {
    var h = new TestHotel();
    h.register("John", "Smith");
    h.clock.plusDays(3);
    var receipt = h.checkOut(1).receipt();
    Check.equal(3L, receipt.days(), "days");
    Check.equal(new BigDecimal("60.00"), receipt.total(), "3 x 20.00");
    Check.equal("John Smith", receipt.stay().guest().toString(), "guest on receipt");
    Check.equal(1, receipt.room().number(), "room on receipt");
  }

  public static void testBusinessRoomTotal() {
    var h = new TestHotel();
    for (String n : new String[] {"Aaa", "Bbb", "Ccc", "Ddd"}) {
      h.register(n, "Test");
    }
    h.clock.plusDays(2);
    var receipt = h.checkOut(4).receipt();
    Check.equal(new BigDecimal("60.00"), receipt.total(), "2 x 30.00");
  }

  public static void testOverMidnightIsOneDay() {
    var h = new TestHotel();
    h.clock.set(LocalDateTime.of(2026, 3, 10, 23, 59));
    h.register("John", "Smith");
    h.clock.set(LocalDateTime.of(2026, 3, 11, 0, 1));
    Check.equal(1L, h.checkOut(1).receipt().days(), "days");
  }

  public static void testPreviewDoesNotCloseStay() {
    var h = new TestHotel();
    h.register("John", "Smith");
    h.clock.plusDays(2);
    var preview = (CheckOutResult.CheckedOut) h.service.previewCheckOut(1);
    Check.equal(new BigDecimal("40.00"), preview.receipt().total(), "preview total");
    Check.equal(1, h.service.occupiedRooms().size(), "guest still in room");
    h.checkOut(1);
    Check.equal(0, h.service.occupiedRooms().size(), "now checked out");
  }

  public static void testPriceChangeDoesNotAffectExistingStay() {
    var rooms =
        new com.hotel.repository.memory.InMemoryRoomRepository(
            6, new BigDecimal("20.00"), new BigDecimal("1.5"));
    var stays = new com.hotel.repository.memory.InMemoryStayRepository(java.util.List.of());
    var clock = new com.hotel.testing.MutableClock(LocalDateTime.of(2026, 3, 10, 12, 0));
    HotelFactory.create(rooms, stays, clock).register("John", "Smith");

    // same stays, but rooms now configured with a new price
    var repriced =
        new com.hotel.repository.memory.InMemoryRoomRepository(
            6, new BigDecimal("50.00"), new BigDecimal("1.5"));
    var service = HotelFactory.create(repriced, stays, clock);
    clock.plusDays(2);
    var receipt = ((CheckOutResult.CheckedOut) service.checkOut(1)).receipt();
    Check.equal(new BigDecimal("40.00"), receipt.total(), "old rate 20.00 x 2");
  }

  public static void testUnknownRoom() {
    var h = new TestHotel();
    h.register("John", "Smith");
    Check.isTrue(h.service.checkOut(99) instanceof CheckOutResult.UnknownRoom, "room 99");
    Check.isTrue(h.service.checkOut(0) instanceof CheckOutResult.UnknownRoom, "room 0");
    Check.isTrue(h.service.previewCheckOut(99) instanceof CheckOutResult.UnknownRoom, "preview 99");
  }

  public static void testFreeRoomAndDoubleCheckOut() {
    var h = new TestHotel();
    Check.isTrue(h.service.checkOut(1) instanceof CheckOutResult.RoomAlreadyFree, "never occupied");
    h.register("John", "Smith");
    h.checkOut(1);
    Check.isTrue(
        h.service.checkOut(1) instanceof CheckOutResult.RoomAlreadyFree, "second check-out");
  }
}
