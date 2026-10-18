package com.hotel.service.report;

import com.hotel.model.Room;

import java.math.BigDecimal;

public record RoomReport(Room room, int timesOccupied, BigDecimal profit) {}
