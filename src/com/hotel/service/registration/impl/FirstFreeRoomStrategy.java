package com.hotel.service.registration.impl;

import com.hotel.service.registration.*;

import com.hotel.model.Room;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Default rule: the free room with the lowest position in the room list, regardless of class. */
public class FirstFreeRoomStrategy implements RoomAllocationStrategy {
  @Override
  public Optional<Room> allocate(List<Room> rooms, Set<Integer> occupiedNumbers) {
    return rooms.stream().filter(r -> !occupiedNumbers.contains(r.number())).findFirst();
  }
}
