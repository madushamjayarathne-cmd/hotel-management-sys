package com.hotel.facade.impl;

import com.hotel.facade.HotelFacade;
import com.hotel.model.Guest;
import com.hotel.model.Stay;
import com.hotel.service.checkout.CheckOutResult;
import com.hotel.service.checkout.CheckOutService;
import com.hotel.service.registration.RegistrationResult;
import com.hotel.service.registration.RegistrationService;
import com.hotel.service.report.ReportService;
import com.hotel.service.report.RoomReport;
import com.hotel.service.room.RoomDetailsService;
import com.hotel.service.room.RoomHistory;

import java.util.List;

public class HotelFacadeImpl implements HotelFacade {
  private final RegistrationService registration;
  private final CheckOutService checkOut;
  private final RoomDetailsService details;
  private final ReportService reports;

  public HotelFacadeImpl(
      RegistrationService registration,
      CheckOutService checkOut,
      RoomDetailsService details,
      ReportService reports) {
    this.registration = registration;
    this.checkOut = checkOut;
    this.details = details;
    this.reports = reports;
  }

  @Override
  public boolean isValidName(String value) {
    return Guest.isValidName(value);
  }

  @Override
  public RegistrationResult register(String name, String surname) {
    return registration.register(name, surname);
  }

  @Override
  public boolean hasFreeRooms() {
    return details.hasFreeRooms();
  }

  @Override
  public boolean hasOccupiedRooms() {
    return !details.occupiedRooms().isEmpty();
  }

  @Override
  public List<Integer> occupiedRoomNumbers() {
    return details.occupiedRooms().stream().map(Stay::roomNumber).toList();
  }

  /** Builds the final receipt without checking the guest out. */
  @Override
  public CheckOutResult previewCheckOut(int roomNumber) {
    return checkOut.previewCheckOut(roomNumber);
  }

  /** Checks the guest out and returns the final receipt. */
  @Override
  public CheckOutResult checkOut(int roomNumber) {
    return checkOut.checkOut(roomNumber);
  }

  @Override
  public List<Stay> occupiedRooms() {
    return details.occupiedRooms();
  }

  @Override
  public List<RoomHistory> history() {
    return details.history();
  }

  @Override
  public List<RoomReport> report() {
    return reports.report();
  }
}
