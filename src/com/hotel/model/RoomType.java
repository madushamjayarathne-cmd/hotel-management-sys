package com.hotel.model;

public enum RoomType {
  STANDARD("STANDARD"),
  BUSINESS("BUSINESS");
  private final String value;

  RoomType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
