package com.hotel.service.room;

import com.hotel.model.Guest;
import com.hotel.model.Room;

import java.util.List;

public record RoomHistory(Room room, boolean occupied, List<Guest> guests) {}
