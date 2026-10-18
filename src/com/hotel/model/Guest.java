package com.hotel.model;

import java.io.Serializable;

public record Guest(String name, String surname) implements Serializable {
  public Guest {
    if (!isValidName(name) || !isValidName(surname)) {
      throw new IllegalArgumentException("Name and surname must contain letters only");
    }
    name = name.trim();
    surname = surname.trim();
  }

  /** Letters only: no digits, spaces or special characters. */
  public static boolean isValidName(String value) {
    return value != null && value.trim().matches("\\p{L}+");
  }

  /** Same person: name and surname equal, ignoring case. */
  public boolean sameAs(Guest other) {
    return name.equalsIgnoreCase(other.name) && surname.equalsIgnoreCase(other.surname);
  }

  @Override
  public String toString() {
    return name + " " + surname;
  }
}
