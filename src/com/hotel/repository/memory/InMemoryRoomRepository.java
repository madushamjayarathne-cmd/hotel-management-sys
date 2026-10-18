package com.hotel.repository.memory;

import com.hotel.model.BusinessRoom;
import com.hotel.model.Room;
import com.hotel.model.StandardRoom;
import com.hotel.repository.RoomRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class InMemoryRoomRepository implements RoomRepository {
  private final List<Room> rooms;

  /** Rooms 1,2... n/2 are standard, the rest are business class. */
  public InMemoryRoomRepository(
      int totalRooms, BigDecimal standardPrice, BigDecimal businessMultiplier) {
    if (totalRooms <= 0 || totalRooms % 2 != 0) {
      throw new IllegalArgumentException("Room count must be positive and even");
    }
    rooms =
        IntStream.rangeClosed(1, totalRooms)
            .<Room>mapToObj(
                i ->
                    i <= totalRooms / 2
                        ? new StandardRoom(i, standardPrice)
                        : new BusinessRoom(i, standardPrice, businessMultiplier))
            .toList();
  }

  @Override
  public List<Room> findAll() {
    return rooms;
  }

  @Override
  public Optional<Room> findByNumber(int number) {
    return rooms.stream().filter(r -> r.number() == number).findFirst();
  }
}
