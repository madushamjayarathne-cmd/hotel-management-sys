package com.hotel.facade;

import com.hotel.model.Stay;
import com.hotel.service.checkout.CheckOutResult;
import com.hotel.service.registration.RegistrationResult;
import com.hotel.service.report.RoomReport;
import com.hotel.service.room.RoomHistory;

import java.util.List;

public interface HotelFacade {
  boolean isValidName(String value);

  RegistrationResult register(String name, String surname);

  boolean hasFreeRooms();

  boolean hasOccupiedRooms();

  List<Integer> occupiedRoomNumbers();

  /** Builds the final receipt without checking the guest out. */
  CheckOutResult previewCheckOut(int roomNumber);

  /** Checks the guest out and returns the final receipt. */
  CheckOutResult checkOut(int roomNumber);

  List<Stay> occupiedRooms();

  List<RoomHistory> history();

  List<RoomReport> report();
}
