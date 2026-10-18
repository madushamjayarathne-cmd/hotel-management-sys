package com.hotel.model;

import java.math.BigDecimal;

public sealed class StandardRoom extends Room permits BusinessRoom {
  private final BigDecimal basePrice;

  public StandardRoom(int number, BigDecimal basePrice) {
    super(number);
    this.basePrice = basePrice;
  }

  @Override
  public BigDecimal price() {
    return basePrice;
  }

  @Override
  public String type() {
    return RoomType.STANDARD.getValue();
  }
}
