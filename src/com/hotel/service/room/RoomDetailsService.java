package com.hotel.service.room;

import com.hotel.model.Room;
import com.hotel.model.Stay;

import java.util.List;

public interface RoomDetailsService {
  boolean hasFreeRooms();

  List<Stay> occupiedRooms();

  List<RoomHistory> history();

  Room room(int number);
}
