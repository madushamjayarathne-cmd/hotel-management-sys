package com.hotel.ui;

import com.hotel.facade.HotelFacade;
import com.hotel.service.checkout.CheckOutResult;
import com.hotel.service.checkout.Receipt;
import com.hotel.service.registration.RegistrationResult;

import java.util.Optional;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ConsoleMenu {
  private static final String BACK = "0";

  private final HotelFacade hotel;
  private final Scanner in = new Scanner(System.in);

  public ConsoleMenu(HotelFacade hotel) {
    this.hotel = hotel;
  }

  public void run() {
    while (true) {
      System.out.println(
          """

          === Hotel ===
          1. Register guest
          2. Check out guest
          3. Occupied rooms
          4. Room history and status
          5. Occupancy report
          0. Exit""");
      String choice = prompt("Choose: ");
      if (choice == null) {
        return;
      }
      switch (choice.trim()) {
        case "0" -> {
          return;
        }
        case "1" -> register();
        case "2" -> checkOut();
        case "3" -> showOccupied();
        case "4" -> showHistory();
        case "5" -> showReport();
        default -> System.out.println("Unknown option.");
      }
    }
  }

  private void register() {
    System.out.println(" == Check in ==");
    if (!hotel.hasFreeRooms()) {
      System.out.println("Warning: all rooms are occupied, no guest can be registered right now.");
      return;
    }
    System.out.println("(enter " + BACK + " at any prompt to go back to the main menu)");
    Optional<String> name = askName("Name: ");
    Optional<String> surname = name.flatMap(n -> askName("Surname: "));
    if (surname.isEmpty()) {
      return;
    }
    System.out.printf("%nConfirm registration of %s %s%n", name.get(), surname.get());
    if (isConfirm("Confirm? (y/n): ")) {
      System.out.println("Registration cancelled.");
      return;
    }
    show(hotel.register(name.get(), surname.get()));
  }

  private void checkOut() {
    System.out.println(" == Check out ==");
    if (!hotel.hasOccupiedRooms()) {
      System.out.println("No rooms are occupied, nothing to check out.");
      return;
    }
    System.out.println(
        "Occupied rooms: "
            + hotel.occupiedRoomNumbers().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", ")));
    boolean finished = false;
    while (!finished) {
      Optional<Integer> room = askRoomNumber();
      if (room.isEmpty()) {
        return;
      }
      finished = confirmCheckOut(room.get());
    }
  }

  private boolean confirmCheckOut(int room) {
    return switch (hotel.previewCheckOut(room)) {
      case CheckOutResult.CheckedOut(var receipt) -> {
        show("Final receipt", receipt);
        if (isConfirm("Confirm check-out? (y/n): ")) {
          System.out.println("Check-out cancelled.");
        } else if (hotel.checkOut(room) instanceof CheckOutResult.CheckedOut(var done)) {
          System.out.println("Check-out completed.");
          show("Receipt", done);
        }
        yield true;
      }
      case CheckOutResult.UnknownRoom ignored -> retry("Check-out failed: no such room.");
      case CheckOutResult.RoomAlreadyFree ignored -> retry("Check-out failed: the room is free.");
    };
  }

  private boolean retry(String message) {
    System.out.println(message + " Try again or enter " + BACK + " to go back.");
    return false;
  }

  private void show(RegistrationResult result) {
    switch (result) {
      case RegistrationResult.Registered(var room, var stay) ->
          System.out.printf(
              "Registered %s in room %d (%s, %s EUR).%n",
              stay.guest(), room.number(), room.type(), room.price());
      case RegistrationResult.AlreadyRegistered(var stay) ->
          System.out.printf(
              "Registration failed: %s is already registered in room %d.%n",
              stay.guest(), stay.roomNumber());
      case RegistrationResult.NoFreeRooms ignored ->
          System.out.println("Registration failed: no empty rooms.");
      case RegistrationResult.InvalidGuest(var reason) ->
          System.out.println("Registration failed: " + reason + ".");
    }
  }

  private void show(String title, Receipt r) {
    System.out.printf("%n=== %s ===%n", title);
    System.out.printf("Guest:      %s%n", r.stay().guest());
    System.out.printf("Room:       %d (%s)%n", r.room().number(), r.room().type());
    System.out.printf("Check-in:   %s%n", r.checkInDate());
    System.out.printf("Check-out:  %s%n", r.checkOutDate());
    System.out.printf("Days:       %d x %s EUR per day%n", r.days(), r.stay().dailyPrice());
    System.out.printf("Total:      %s EUR%n", r.total());
  }

  private void showOccupied() {
    System.out.println(" == Guest Details ==");
    var stays = hotel.occupiedRooms();
    if (stays.isEmpty()) {
      System.out.println("No occupied rooms.");
    }
    stays.forEach(s -> System.out.printf("Room %d: %s%n", s.roomNumber(), s.guest()));
  }

  private void showHistory() {
    System.out.println(" == Room history ==");
    for (var h : hotel.history()) {
      System.out.printf(
          "Room %d (%s) - %s%n",
          h.room().number(), h.room().type(), h.occupied() ? "occupied" : "free");
      if (h.guests().isEmpty()) {
        System.out.println("   no guests yet");
      }
      for (int i = 0; i < h.guests().size(); i++) {
        System.out.printf("   %d. %s%n", i + 1, h.guests().get(i));
      }
    }
  }

  private void showReport() {
    System.out.println(" == Profit report ==");
    System.out.printf("%-6s %-9s %s%n", "Room", "Times", "Profit (EUR)");
    for (var r : hotel.report()) {
      System.out.printf("%-6d %-9d %s%n", r.room().number(), r.timesOccupied(), r.profit());
    }
  }

  private Optional<String> askName(String text) {
    return ask(
        text,
        hotel::isValidName,
        "Invalid input: it cannot be empty and must contain letters only.");
  }

  private Optional<Integer> askRoomNumber() {
    return ask(
            "Room number (" + BACK + " = back): ",
            ConsoleMenu::isNumber,
            "Invalid room number, try again or enter " + BACK + " to go back.")
        .map(Integer::parseInt);
  }

  private Optional<String> ask(String text, Predicate<String> valid, String errorMessage) {
    while (true) {
      String value = prompt(text);
      if (value == null || value.trim().equals(BACK)) {
        return Optional.empty();
      }
      if (valid.test(value)) {
        return Optional.of(value.trim());
      }
      System.out.println(errorMessage);
    }
  }

  private boolean isConfirm(String question) {
    String answer = prompt(question);
    return answer == null || !answer.trim().equalsIgnoreCase("y");
  }

  private static boolean isNumber(String text) {
    try {
      Integer.parseInt(text.trim());
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private String prompt(String text) {
    System.out.print(text);
    return in.hasNextLine() ? in.nextLine() : null;
  }
}
