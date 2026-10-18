package com.hotel.model;

import com.hotel.testing.Check;

public class GuestTest {
  public static void testValidGuestIsTrimmed() {
    var g = new Guest("  John ", " Smith  ");
    Check.equal("John", g.name(), "name");
    Check.equal("Smith", g.surname(), "surname");
    Check.equal("John Smith", g.toString(), "toString");
  }

  public static void testUnicodeLettersAllowed() {
    Check.isTrue(Guest.isValidName("Åsa"), "Åsa");
    Check.isTrue(Guest.isValidName("Jürgen"), "Jürgen");
  }

  public static void testInvalidNamesRejected() {
    for (String bad :
        new String[] {
          null, "", "   ", "J0hn", "123", "Sm!th", "Anna-Maria", "Mary Ann", "O'Neil"
        }) {
      Check.isTrue(!Guest.isValidName(bad), "should be invalid: " + bad);
      Check.throwsException(
          IllegalArgumentException.class, () -> new Guest(bad, "Smith"), "name " + bad);
      Check.throwsException(
          IllegalArgumentException.class, () -> new Guest("John", bad), "surname " + bad);
    }
  }

  public static void testSameAsIgnoresCase() {
    Check.isTrue(new Guest("John", "Smith").sameAs(new Guest("jOHN", "SMITH")), "same person");
    Check.isTrue(
        !new Guest("John", "Smith").sameAs(new Guest("John", "Smyth")), "different surname");
    Check.isTrue(!new Guest("John", "Smith").sameAs(new Guest("Jon", "Smith")), "different name");
  }
}
