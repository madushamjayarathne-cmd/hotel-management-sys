package com.hotel.service.checkout;

import com.hotel.model.Room;
import com.hotel.model.Stay;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public record Receipt(Stay stay, Room room, Instant checkOut, long days, BigDecimal total) {
  public LocalDate checkInDate() {
    return LocalDate.ofInstant(stay.checkIn(), ZoneId.systemDefault());
  }

  public LocalDate checkOutDate() {
    return LocalDate.ofInstant(checkOut, ZoneId.systemDefault());
  }
}
