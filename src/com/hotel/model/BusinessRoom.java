package com.hotel.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class BusinessRoom extends StandardRoom {
  private final BigDecimal multiplier;

  public BusinessRoom(int number, BigDecimal basePrice, BigDecimal multiplier) {
    super(number, basePrice);
    this.multiplier = multiplier;
  }

  @Override
  public BigDecimal price() {
    return super.price().multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
  }

  @Override
  public String type() {
    return RoomType.BUSINESS.getValue();
  }
}
