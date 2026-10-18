package com.hotel.facade;

import com.hotel.facade.impl.HotelFacadeImpl;
import com.hotel.model.Room;
import com.hotel.model.Stay;
import com.hotel.service.checkout.CheckOutResult;
import com.hotel.service.checkout.CheckOutService;
import com.hotel.service.registration.RegistrationService;
import com.hotel.service.registration.RegistrationResult;
import com.hotel.service.report.ReportService;
import com.hotel.service.report.RoomReport;
import com.hotel.service.room.RoomDetailsService;
import com.hotel.service.room.RoomHistory;
import com.hotel.testing.Check;

import java.util.ArrayList;
import java.util.List;

/** The facade only delegates; each use case is replaced by a mock here. */
public class HotelFacadeImplTest {
  private static final List<String> calls = new ArrayList<>();

  private static HotelFacade facade(boolean free, List<Stay> occupied) {
    calls.clear();
    RegistrationService registration =
        (n, s) -> {
          calls.add("register " + n + " " + s);
          return new RegistrationResult.NoFreeRooms();
        };
    CheckOutService checkOut =
        new CheckOutService() {
          public CheckOutResult previewCheckOut(int room) {
            calls.add("preview " + room);
            return new CheckOutResult.UnknownRoom();
          }

          public CheckOutResult checkOut(int room) {
            calls.add("checkOut " + room);
            return new CheckOutResult.RoomAlreadyFree();
          }
        };
    RoomDetailsService details =
        new RoomDetailsService() {
          public boolean hasFreeRooms() {
            return free;
          }

          public List<Stay> occupiedRooms() {
            return occupied;
          }

          public List<RoomHistory> history() {
            return List.of();
          }

          public Room room(int number) {
            throw new UnsupportedOperationException();
          }
        };
    ReportService reports = () -> List.<RoomReport>of();
    return new HotelFacadeImpl(registration, checkOut, details, reports);
  }

  public static void testDelegatesRegistrationAndCheckOut() {
    var f = facade(true, List.of());
    Check.isTrue(f.register("Ann", "Lee") instanceof RegistrationResult.NoFreeRooms, "result");
    Check.isTrue(f.previewCheckOut(2) instanceof CheckOutResult.UnknownRoom, "preview result");
    Check.isTrue(f.checkOut(3) instanceof CheckOutResult.RoomAlreadyFree, "checkout result");
    Check.equal(List.of("register Ann Lee", "preview 2", "checkOut 3"), calls, "calls");
  }

  public static void testAvailabilityComesFromRoomDetails() {
    Check.isTrue(!facade(false, List.of()).hasFreeRooms(), "no free rooms");
    Check.isTrue(!facade(true, List.of()).hasOccupiedRooms(), "nobody checked in");
    Check.equal(0, facade(true, List.of()).report().size(), "report delegated");
  }
}
