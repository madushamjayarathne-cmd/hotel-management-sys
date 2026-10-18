package com.hotel.service;

import com.hotel.facade.HotelFacade;
import com.hotel.service.checkout.*;
import com.hotel.service.registration.*;
import com.hotel.testing.Check;

import java.math.BigDecimal;

public class QueriesTest {
  public static void testOccupiedRoomsListsGuestsByRoom() {
    var h = new TestHotel();
    h.register("Aaa", "One");
    h.register("Bbb", "Two");
    h.register("Ccc", "Three");
    h.checkOut(2);
    var occupied = h.service.occupiedRooms();
    Check.equal(2, occupied.size(), "count");
    Check.equal(1, occupied.get(0).roomNumber(), "first room");
    Check.equal("Aaa One", occupied.get(0).guest().toString(), "first guest");
    Check.equal(3, occupied.get(1).roomNumber(), "second room");
  }

  public static void testOccupiedRoomsEmptyInNewHotel() {
    Check.isTrue(new TestHotel().service.occupiedRooms().isEmpty(), "empty");
  }

  public static void testHistoryListsAllGuestsInOrderWithStatus() {
    var h = new TestHotel();
    h.register("Aaa", "One");
    h.checkOut(1);
    h.register("Bbb", "Two");
    h.checkOut(1);
    h.register("Ccc", "Three");
    var history = h.service.history();
    Check.equal(6, history.size(), "all rooms listed");
    var room1 = history.getFirst();
    Check.equal(3, room1.guests().size(), "three guests in room 1");
    Check.equal("Aaa One", room1.guests().get(0).toString(), "first");
    Check.equal("Bbb Two", room1.guests().get(1).toString(), "second");
    Check.equal("Ccc Three", room1.guests().get(2).toString(), "third");
    Check.isTrue(room1.occupied(), "room 1 occupied");
    Check.isTrue(!history.get(1).occupied(), "room 2 free");
    Check.isTrue(history.get(1).guests().isEmpty(), "room 2 no guests");
  }

  public static void testHistoryStatusFreeAfterCheckOut() {
    var h = new TestHotel();
    h.register("Aaa", "One");
    h.checkOut(1);
    Check.isTrue(!h.service.history().getFirst().occupied(), "free");
    Check.equal(1, h.service.history().getFirst().guests().size(), "guest kept in history");
  }

  public static void testReportSortedByOccupancyDescending() {
    var h = new TestHotel();
    // room 1: 3 stays, room 2: 2 stays, room 3: 1 stay
    h.register("Aaa", "One");
    h.register("Bbb", "Two");
    h.register("Ccc", "Three");
    h.checkOut(1);
    h.checkOut(2);
    h.register("Ddd", "Four");
    h.register("Eee", "Five");
    h.checkOut(1);
    h.register("Fff", "Six");
    var report = h.service.report();
    Check.equal(6, report.size(), "all rooms");
    Check.equal(1, report.get(0).room().number(), "most occupied first");
    Check.equal(3, report.get(0).timesOccupied(), "3 times");
    Check.equal(2, report.get(1).room().number(), "second");
    Check.equal(2, report.get(1).timesOccupied(), "2 times");
    Check.equal(3, report.get(2).room().number(), "third");
    for (int i = 1; i < report.size(); i++) {
      Check.isTrue(
          report.get(i - 1).timesOccupied() >= report.get(i).timesOccupied(), "descending at " + i);
    }
  }

  public static void testReportTiesOrderedByRoomNumber() {
    var h = new TestHotel();
    var report = h.service.report();
    for (int i = 0; i < 6; i++) {
      Check.equal(i + 1, report.get(i).room().number(), "room order with no stays");
      Check.equal(0, report.get(i).timesOccupied(), "zero stays");
    }
  }

  public static void testReportProfitSumsStaysUsingPricesAtTheTime() {
    var rooms =
        new com.hotel.repository.memory.InMemoryRoomRepository(
            6, new BigDecimal("50.00"), new BigDecimal("1.5"));
    var stays = new com.hotel.repository.memory.InMemoryStayRepository(java.util.List.of());
    var clock = new com.hotel.testing.MutableClock(java.time.LocalDateTime.of(2026, 3, 10, 12, 0));
    var oldService =
        HotelFacade.create(
            new com.hotel.repository.memory.InMemoryRoomRepository(
                6, new BigDecimal("20.00"), new BigDecimal("1.5")),
            stays,
            clock);
    oldService.register("Aaa", "One");
    clock.plusDays(2);
    oldService.checkOut(1);
    var newService = HotelFacade.create(rooms, stays, clock);
    newService.register("Bbb", "Two");
    clock.plusDays(1);
    newService.checkOut(1); // 1 day x 50

    var room1 =
        newService.report().stream().filter(r -> r.room().number() == 1).findFirst().orElseThrow();
    Check.equal(2, room1.timesOccupied(), "two stays");
    Check.equal(new BigDecimal("90.00"), room1.profit(), "40.00 at old price + 50.00 at new price");
  }

  public static void testReportIncludesActiveStayUpToToday() {
    var h = new TestHotel();
    h.register("Aaa", "One");
    h.clock.plusDays(2);
    var room1 = h.service.report().getFirst();
    Check.equal(new BigDecimal("40.00"), room1.profit(), "2 days so far");
  }

  public static void testReportProfitForBusinessRoom() {
    var h = new TestHotel();
    for (String n : new String[] {"Aaa", "Bbb", "Ccc", "Ddd"}) {
      h.register(n, "Test");
    }
    h.clock.plusDays(2);
    h.checkOut(4);
    var room4 =
        h.service.report().stream().filter(r -> r.room().number() == 4).findFirst().orElseThrow();
    Check.equal(new BigDecimal("60.00"), room4.profit(), "2 x 30.00");
  }

  public static void testEmptyReportProfitIsZero() {
    var h = new TestHotel();
    Check.equal(new BigDecimal("0.00"), h.service.report().getFirst().profit(), "zero profit");
  }
}
