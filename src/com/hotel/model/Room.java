package com.hotel.model;

import java.math.BigDecimal;

public abstract sealed class Room permits StandardRoom {
  private final int number;

  protected Room(int number) {
    this.number = number;
  }

  public int number() {
    return number;
  }

  public abstract BigDecimal price();

  public abstract String type();
}
