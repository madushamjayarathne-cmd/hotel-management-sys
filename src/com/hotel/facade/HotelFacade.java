package com.hotel.facade;

import com.hotel.model.Guest;
import com.hotel.model.Stay;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.StayRepository;
import com.hotel.service.checkout.CheckOutResult;
import com.hotel.service.checkout.CheckOutService;
import com.hotel.service.registration.RegistrationResult;
import com.hotel.service.registration.RegistrationService;
import com.hotel.service.report.ReportService;
import com.hotel.service.report.RoomReport;
import com.hotel.service.room.RoomDetailsService;
import com.hotel.service.room.RoomHistory;

import java.time.Clock;
import java.util.List;

/**
 * The single entry point for any front end (console, Swing, REST...). It exposes the use cases and
 * returns plain results; it never reads input or prints.
 */
public class HotelFacade {
  private final RegistrationService registration;
  private final CheckOutService checkOut;
  private final RoomDetailsService details;
  private final ReportService reports;

  public HotelFacade(
      RegistrationService registration,
      CheckOutService checkOut,
      RoomDetailsService details,
      ReportService reports) {
    this.registration = registration;
    this.checkOut = checkOut;
    this.details = details;
    this.reports = reports;
  }

  /** Wires all services over the given repositories. The clock is injectable for tests. */
  public static HotelFacade create(RoomRepository rooms, StayRepository stays, Clock clock) {
    return new HotelFacade(
        new RegistrationService(rooms, stays, clock),
        new CheckOutService(rooms, stays, clock),
        new RoomDetailsService(rooms, stays),
        new ReportService(rooms, stays, clock));
  }

  public static HotelFacade create(RoomRepository rooms, StayRepository stays) {
    return create(rooms, stays, Clock.systemDefaultZone());
  }

  public boolean isValidName(String value) {
    return Guest.isValidName(value);
  }

  public RegistrationResult register(String name, String surname) {
    return registration.register(name, surname);
  }

  public boolean hasOccupiedRooms() {
    return !details.occupiedRooms().isEmpty();
  }

  public List<Integer> occupiedRoomNumbers() {
    return details.occupiedRooms().stream().map(Stay::roomNumber).toList();
  }

  /** Builds the final receipt without checking the guest out. */
  public CheckOutResult previewCheckOut(int roomNumber) {
    return checkOut.previewCheckOut(roomNumber);
  }

  /** Checks the guest out and returns the final receipt. */
  public CheckOutResult checkOut(int roomNumber) {
    return checkOut.checkOut(roomNumber);
  }

  public List<Stay> occupiedRooms() {
    return details.occupiedRooms();
  }

  public List<RoomHistory> history() {
    return details.history();
  }

  public List<RoomReport> report() {
    return reports.report();
  }
}
