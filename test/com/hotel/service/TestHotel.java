package com.hotel.service;

import com.hotel.facade.HotelFacade;
import com.hotel.service.checkout.CheckOutResult;
import com.hotel.service.registration.RegistrationResult;
import com.hotel.repository.memory.InMemoryRoomRepository;
import com.hotel.repository.memory.InMemoryStayRepository;
import com.hotel.testing.MutableClock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Builds a 6-room hotel (20 / 30 EUR per day) with a controllable clock. */
class TestHotel {
  final MutableClock clock = new MutableClock(LocalDateTime.of(2026, 3, 10, 12, 0));
  final HotelFacade service;

  TestHotel() {
    this(new BigDecimal("20.00"));
  }

  TestHotel(BigDecimal standardPrice) {
    this(new InMemoryRoomRepository(6, standardPrice, new BigDecimal("1.5")));
  }

  TestHotel(InMemoryRoomRepository rooms) {
    service = HotelFacade.create(rooms, new InMemoryStayRepository(List.of()), clock);
  }

  RegistrationResult.Registered register(String name, String surname) {
    return (RegistrationResult.Registered) service.register(name, surname);
  }

  CheckOutResult.CheckedOut checkOut(int room) {
    return (CheckOutResult.CheckedOut) service.checkOut(room);
  }
}
