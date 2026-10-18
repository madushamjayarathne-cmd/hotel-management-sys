package com.hotel.service.room.impl;

import com.hotel.service.room.*;

import com.hotel.model.Room;
import com.hotel.model.Stay;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.StayRepository;

import java.util.Comparator;
import java.util.List;

/** Room availability, details and occupancy history. */
public class RoomDetailsServiceImpl implements RoomDetailsService {
  private final RoomRepository rooms;
  private final StayRepository stays;

  public RoomDetailsServiceImpl(RoomRepository rooms, StayRepository stays) {
    this.rooms = rooms;
    this.stays = stays;
  }

  @Override
  public boolean hasFreeRooms() {
    return stays.findActive().size() < rooms.findAll().size();
  }

  @Override
  public List<Stay> occupiedRooms() {
    return stays.findActive().stream().sorted(Comparator.comparingInt(Stay::roomNumber)).toList();
  }

  @Override
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

  @Override
  public Room room(int number) {
    return rooms.findByNumber(number).orElseThrow();
  }
}
