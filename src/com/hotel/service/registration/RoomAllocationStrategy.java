package com.hotel.service.registration;

import com.hotel.model.Room;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Rule for choosing which free room a new guest gets. */
public interface RoomAllocationStrategy {
  Optional<Room> allocate(List<Room> rooms, Set<Integer> occupiedNumbers);
}
