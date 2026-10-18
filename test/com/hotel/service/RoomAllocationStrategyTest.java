package com.hotel.service;

import com.hotel.HotelFactory;
import com.hotel.model.Room;
import com.hotel.repository.memory.InMemoryRoomRepository;
import com.hotel.repository.memory.InMemoryStayRepository;
import com.hotel.service.registration.impl.FirstFreeRoomStrategy;
import com.hotel.service.registration.RegistrationResult;
import com.hotel.service.registration.RoomAllocationStrategy;
import com.hotel.testing.Check;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Set;

public class RoomAllocationStrategyTest {
  private static List<Room> rooms() {
    return new InMemoryRoomRepository(6, new BigDecimal("20.00"), new BigDecimal("1.5")).findAll();
  }

  public static void testFirstFreePicksLowestFreeRoom() {
    var room = new FirstFreeRoomStrategy().allocate(rooms(), Set.of(1, 2));
    Check.equal(3, room.orElseThrow().number(), "room 3");
  }

  public static void testFirstFreeEmptyWhenAllOccupied() {
    var room = new FirstFreeRoomStrategy().allocate(rooms(), Set.of(1, 2, 3, 4, 5, 6));
    Check.isTrue(room.isEmpty(), "no room");
  }

  public static void testCustomStrategyIsUsedByRegistration() {
    RoomAllocationStrategy lastFree =
        (all, busy) -> all.reversed().stream().filter(r -> !busy.contains(r.number())).findFirst();
    var hotel =
        HotelFactory.create(
            new InMemoryRoomRepository(6, new BigDecimal("20.00"), new BigDecimal("1.5")),
            new InMemoryStayRepository(List.of()),
            Clock.systemDefaultZone(),
            lastFree);
    var first = (RegistrationResult.Registered) hotel.register("Ann", "Test");
    var second = (RegistrationResult.Registered) hotel.register("Bob", "Test");
    Check.equal(6, first.room().number(), "highest room first");
    Check.equal(5, second.room().number(), "then next highest");
  }

  public static void testNoFreeRoomFromStrategyGivesNoFreeRooms() {
    var hotel =
        HotelFactory.create(
            new InMemoryRoomRepository(6, new BigDecimal("20.00"), new BigDecimal("1.5")),
            new InMemoryStayRepository(List.of()),
            Clock.systemDefaultZone(),
            (all, busy) -> java.util.Optional.empty());
    Check.isTrue(
        hotel.register("Ann", "Test") instanceof RegistrationResult.NoFreeRooms, "no free rooms");
  }
}
