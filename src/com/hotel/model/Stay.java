package com.hotel.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * One guest's stay in one room; checkOut == null while the guest lives there. The daily price is the
 * room's rate at registration, kept so later price changes do not alter past profit.
 */
public record Stay(
    long id, int roomNumber, Guest guest, BigDecimal dailyPrice, Instant checkIn, Instant checkOut)
    implements Serializable {

  public Stay {
    Objects.requireNonNull(dailyPrice, "dailyPrice");
  }

  public boolean active() {
    return checkOut == null;
  }

  /** Calendar days from check-in to the given moment; a same-day stay counts as 1 day. */
  public long days(Instant end) {
    ZoneId zone = ZoneId.systemDefault();
    LocalDate from = LocalDate.ofInstant(checkIn, zone);
    LocalDate to = LocalDate.ofInstant(end, zone);
    return Math.max(1, ChronoUnit.DAYS.between(from, to));
  }

  /** Daily rate × days stayed up to the given moment. */
  public BigDecimal total(Instant end) {
    return dailyPrice.multiply(BigDecimal.valueOf(days(end)));
  }

  public Stay checkedOut(Instant when) {
    return new Stay(id, roomNumber, guest, dailyPrice, checkIn, when);
  }

  public Stay withId(long newId) {
    return new Stay(newId, roomNumber, guest, dailyPrice, checkIn, checkOut);
  }
}
