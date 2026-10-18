package com.hotel.service.room;

import com.hotel.model.Guest;
import com.hotel.model.Room;
import com.hotel.model.Stay;

import java.util.List;

/** A room's status and its stays, oldest check-in first. */
public record RoomHistory(Room room, boolean occupied, List<Stay> stays) {
  public List<Guest> guests() {
    return stays.stream().map(Stay::guest).toList();
  }
}
