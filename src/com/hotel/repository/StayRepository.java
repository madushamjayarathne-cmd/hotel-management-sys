package com.hotel.repository;

import com.hotel.model.Stay;

import java.util.List;
import java.util.Optional;

/** Persistence port; implement with JDBC/JPA to move to a database. */
public interface StayRepository {
    /** Inserts (id == 0) or updates a stay and returns the stored one. */
    Stay save(Stay stay);

    Optional<Stay> findActiveByRoom(int roomNumber);

    List<Stay> findActive();

    /** All stays of the room in check-in order. */
    List<Stay> findByRoom(int roomNumber);
}
