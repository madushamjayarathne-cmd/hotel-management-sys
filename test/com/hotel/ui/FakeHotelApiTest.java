package com.hotel.ui;

import com.hotel.facade.HotelFacade;
import com.hotel.model.Stay;
import com.hotel.service.checkout.CheckOutResult;
import com.hotel.service.registration.RegistrationResult;
import com.hotel.service.report.RoomReport;
import com.hotel.service.room.RoomHistory;
import com.hotel.testing.Check;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/** Shows the menu works with any HotelApi implementation, not only HotelFacade. */
public class FakeHotelApiTest {
  private static class FakeApi implements HotelFacade {
    final List<String> registered = new ArrayList<>();

    public boolean isValidName(String value) {
      return value != null && value.matches("\\p{L}+");
    }

    public RegistrationResult register(String name, String surname) {
      registered.add(name + " " + surname);
      return new RegistrationResult.NoFreeRooms();
    }

    public boolean hasFreeRooms() {
      return true;
    }

    public boolean hasOccupiedRooms() {
      return false;
    }

    public List<Integer> occupiedRoomNumbers() {
      return List.of();
    }

    public CheckOutResult previewCheckOut(int roomNumber) {
      return new CheckOutResult.UnknownRoom();
    }

    public CheckOutResult checkOut(int roomNumber) {
      return new CheckOutResult.UnknownRoom();
    }

    public List<Stay> occupiedRooms() {
      return List.of();
    }

    public List<RoomHistory> history() {
      return List.of();
    }

    public List<RoomReport> report() {
      return List.of();
    }
  }

  public static void testMenuTalksOnlyToTheApi() {
    InputStream oldIn = System.in;
    PrintStream oldOut = System.out;
    var buffer = new ByteArrayOutputStream();
    var api = new FakeApi();
    try {
      System.setIn(new ByteArrayInputStream("1\nJohn\nSmith\ny\n0\n".getBytes()));
      System.setOut(new PrintStream(buffer, true));
      new ConsoleMenu(api).run();
    } finally {
      System.setIn(oldIn);
      System.setOut(oldOut);
    }
    Check.equal(List.of("John Smith"), api.registered, "menu called the fake api");
  }
}
