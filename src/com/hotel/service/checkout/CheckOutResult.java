package com.hotel.service.checkout;

public sealed interface CheckOutResult {
  record CheckedOut(Receipt receipt) implements CheckOutResult {}

  record UnknownRoom() implements CheckOutResult {}

  record RoomAlreadyFree() implements CheckOutResult {}
}
