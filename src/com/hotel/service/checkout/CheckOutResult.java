package com.hotel.service.checkout;

public sealed interface CheckOutResult {
    /** The final receipt; the stay is closed only for results of {@code checkOut}. */
    record CheckedOut(Receipt receipt) implements CheckOutResult {}

    record UnknownRoom() implements CheckOutResult {}

    record RoomAlreadyFree() implements CheckOutResult {}
}
