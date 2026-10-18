package com.hotel.service.checkout;

import com.hotel.model.Room;
import com.hotel.model.Stay;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.StayRepository;

import java.time.Clock;
import java.time.Instant;

/** Check-out and final receipt calculation. */
public class CheckOutService {
  private final RoomRepository rooms;
  private final StayRepository stays;
  private final Clock clock;

  public CheckOutService(RoomRepository rooms, StayRepository stays, Clock clock) {
    this.rooms = rooms;
    this.stays = stays;
    this.clock = clock;
  }

  /** Calculates the final receipt without closing the stay. */
  public CheckOutResult previewCheckOut(int roomNumber) {
    return findCheckOut(roomNumber, false);
  }

  /** Closes the stay and returns the final receipt. */
  public CheckOutResult checkOut(int roomNumber) {
    return findCheckOut(roomNumber, true);
  }

  private CheckOutResult findCheckOut(int roomNumber, boolean complete) {
    var room = rooms.findByNumber(roomNumber);
    return room.map(
            value ->
                stays
                    .findActiveByRoom(roomNumber)
                    .<CheckOutResult>map(
                        s -> {
                          Instant now = clock.instant();
                          Stay stay = complete ? stays.save(s.checkedOut(now)) : s;
                          return new CheckOutResult.CheckedOut(receipt(stay, value, now));
                        })
                    .orElseGet(CheckOutResult.RoomAlreadyFree::new))
        .orElseGet(CheckOutResult.UnknownRoom::new);
  }

  private Receipt receipt(Stay stay, Room room, Instant end) {
    long days = stay.days(end);
    return new Receipt(stay, room, end, days, stay.total(end));
  }
}
