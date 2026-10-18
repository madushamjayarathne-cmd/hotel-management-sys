package com.hotel.ui;

import com.hotel.HotelFactory;
import com.hotel.repository.memory.InMemoryRoomRepository;
import com.hotel.repository.memory.InMemoryStayRepository;
import com.hotel.testing.Check;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;

/** Drives the menu with scripted input and checks what it prints. */
public class ConsoleMenuTest {
  private static String run(String... lines) {
    InputStream oldIn = System.in;
    PrintStream oldOut = System.out;
    var buffer = new ByteArrayOutputStream();
    try {
      System.setIn(new ByteArrayInputStream((String.join("\n", lines) + "\n").getBytes()));
      System.setOut(new PrintStream(buffer, true));
      var facade =
          HotelFactory.create(
              new InMemoryRoomRepository(6, new BigDecimal("20.00"), new BigDecimal("1.5")),
              new InMemoryStayRepository(List.of()));
      new ConsoleMenu(facade).run();
    } finally {
      System.setIn(oldIn);
      System.setOut(oldOut);
    }
    return buffer.toString();
  }

  public static void testRegisterWithConfirmation() {
    String out = run("1", "John", "Smith", "y", "0");
    Check.contains(out, "Confirm registration of John Smith", "confirmation shown");
    Check.contains(out, "Registered John Smith in room 1 (STANDARD, 20.00 EUR)", "success message");
  }

  public static void testRegisterCancelled() {
    String out = run("1", "John", "Smith", "n", "3", "0");
    Check.contains(out, "Registration cancelled", "cancel message");
    Check.contains(out, "No occupied rooms", "nobody registered");
  }

  public static void testInvalidNamesAreAskedAgain() {
    String out = run("1", "J0hn", "", "John", "Sm!th", "Smith", "y", "0");
    Check.contains(out, "must contain letters only", "validation message");
    Check.contains(out, "Registered John Smith", "registered with valid input");
  }

  public static void testDuplicateGuestMessage() {
    String out = run("1", "John", "Smith", "y", "1", "john", "smith", "y", "0");
    Check.contains(out, "John Smith is already registered in room 1", "duplicate message");
  }

  public static void testNoFreeRoomsMessage() {
    String out =
        run(
            "1", "Aaa", "Test", "y", "1", "Bbb", "Test", "y", "1", "Ccc", "Test", "y", "1", "Ddd",
            "Test", "y", "1", "Eee", "Test", "y", "1", "Fff", "Test", "y", "1", "Ggg", "0");
    Check.contains(out, "all rooms are occupied", "warning shown");
    Check.isTrue(!out.contains("Registration failed"), "no registration attempted");
    Check.equal(6, out.split("Surname:", -1).length - 1, "only six guests were asked for a name");
  }

  public static void testCheckOutWhenNothingOccupied() {
    String out = run("2", "0");
    Check.contains(out, "No rooms are occupied, nothing to check out", "warning");
    Check.isTrue(!out.contains("Room number:"), "must not ask for a room number");
  }

  public static void testCheckOutShowsReceiptAndNeedsConfirmation() {
    String out = run("1", "John", "Smith", "y", "2", "1", "n", "3", "2", "1", "y", "3", "0");
    Check.contains(out, "Final receipt", "receipt shown");
    Check.contains(out, "1 x 20.00 EUR per day", "days and rate");
    Check.contains(out, "Total:      20.00 EUR", "total");
    Check.contains(out, "Check-out cancelled", "declined confirmation");
    Check.contains(out, "Room 1: John Smith", "guest still in room after cancel");
    Check.contains(out, "Check-out completed", "confirmed");
    Check.contains(out, "No occupied rooms", "room freed after confirm");
  }

  public static void testCheckOutRetriesUntilValidRoom() {
    String out = run("1", "John", "Smith", "y", "2", "abc", "99", "2", "1", "y", "3", "0");
    Check.contains(out, "Invalid room number", "not a number");
    Check.contains(out, "no such room", "unknown room");
    Check.contains(out, "the room is free", "free room");
    Check.contains(out, "Check-out completed", "finally checked out room 1");
  }

  public static void testCheckOutBackReturnsToMainMenu() {
    String out = run("1", "John", "Smith", "y", "2", "0", "3", "0");
    Check.isTrue(!out.contains("Final receipt"), "nothing checked out");
    Check.contains(out, "Room 1: John Smith", "guest still in room, main menu works");
  }

  public static void testCheckOutBackAfterMistake() {
    String out = run("1", "John", "Smith", "y", "2", "abc", "0", "3", "0");
    Check.contains(out, "Invalid room number", "mistake reported");
    Check.contains(out, "Room 1: John Smith", "back to main menu without checking out");
  }

  public static void testCheckOutListsOccupiedRooms() {
    String out = run("1", "Aaa", "One", "y", "1", "Bbb", "Two", "y", "2", "0", "0");
    Check.contains(out, "Occupied rooms: 1, 2", "hint shown");
  }

  public static void testRegisterBackFromNameOrSurname() {
    String out = run("1", "0", "1", "John", "0", "3", "0");
    Check.contains(out, "No occupied rooms", "nobody registered after going back");
    Check.isTrue(!out.contains("Confirm registration"), "no confirmation shown");
  }

  public static void testCheckOutFreeRoom() {
    String out = run("1", "John", "Smith", "y", "2", "2", "0");
    Check.contains(out, "the room is free", "room 2 is free");
  }

  public static void testOccupiedRoomsList() {
    String out = run("1", "Aaa", "One", "y", "1", "Bbb", "Two", "y", "3", "0");
    Check.contains(out, "Room 1: Aaa One", "room 1");
    Check.contains(out, "Room 2: Bbb Two", "room 2");
  }

  public static void testHistoryShowsStatusAndGuests() {
    String out = run("1", "Aaa", "One", "y", "2", "1", "y", "1", "Bbb", "Two", "y", "4", "0");
    Check.contains(out, "Room 1 (STANDARD) - occupied", "status");
    Check.contains(out, "1. Aaa One", "first guest");
    Check.contains(out, "2. Bbb Two", "second guest");
    Check.contains(out, "Room 2 (STANDARD) - free", "free room");
    Check.contains(out, "Room 4 (BUSINESS) - free", "business room");
  }

  public static void testReportShowsRoomsSortedByOccupancy() {
    String out =
        run(
            "1", "Aaa", "One", "y", "2", "1", "y", "1", "Bbb", "Two", "y", "1", "Ccc", "Three", "y",
            "5", "0");
    String report = out.substring(out.indexOf("Room   Times"));
    Check.isTrue(
        report.indexOf("\n1 ") < report.indexOf("\n2 "), "room 1 (2 stays) listed before room 2");
    Check.contains(report, "Profit (EUR)", "header");
  }

  public static void testUnknownOptionAndEndOfInput() {
    Check.contains(run("9", "0"), "Unknown option", "unknown option");
    Check.contains(run("1"), "Name:", "ends cleanly when input stops");
  }
}
