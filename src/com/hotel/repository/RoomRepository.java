package com.hotel.repository;

import com.hotel.model.Room;

import java.util.List;
import java.util.Optional;

public interface RoomRepository {
    List<Room> findAll();

    Optional<Room> findByNumber(int number);
}
