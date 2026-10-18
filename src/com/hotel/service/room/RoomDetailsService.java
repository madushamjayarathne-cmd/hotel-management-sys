package com.hotel.service.room;

import com.hotel.model.Room;
import com.hotel.model.Stay;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.StayRepository;

import java.util.Comparator;
import java.util.List;

/** Room availability, details and occupancy history. */
public class RoomDetailsService {
  private final RoomRepository rooms;
  private final StayRepository stays;

  public RoomDetailsService(RoomRepository rooms, StayRepository stays) {
    this.rooms = rooms;
    this.stays = stays;
  }

  public List<Stay> occupiedRooms() {
    return stays.findActive().stream().sorted(Comparator.comparingInt(Stay::roomNumber)).toList();
  }

  public List<RoomHistory> history() {
    return rooms.findAll().stream()
        .map(
            r -> {
              List<Stay> list = stays.findByRoom(r.number());
              return new RoomHistory(
                  r, list.stream().anyMatch(Stay::active), list.stream().map(Stay::guest).toList());
            })
        .toList();
  }

  public Room room(int number) {
    return rooms.findByNumber(number).orElseThrow();
  }
}
