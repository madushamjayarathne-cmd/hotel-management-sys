package com.hotel.service.registration;

import com.hotel.model.Guest;
import com.hotel.model.Stay;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.StayRepository;

import java.time.Clock;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Guest registration (check-in). */
public class RegistrationService {
  private final RoomRepository rooms;
  private final StayRepository stays;
  private final Clock clock;

  public RegistrationService(RoomRepository rooms, StayRepository stays, Clock clock) {
    this.rooms = rooms;
    this.stays = stays;
    this.clock = clock;
  }

  public RegistrationResult register(String name, String surname) {
    Guest guest;
    try {
      guest = new Guest(name, surname);
    } catch (IllegalArgumentException e) {
      return new RegistrationResult.InvalidGuest(e.getMessage());
    }
    List<Stay> currentGuest = stays.findActive();
    var existing = currentGuest.stream().filter(st -> st.guest().sameAs(guest)).findFirst();
    if (existing.isPresent()) {
      return new RegistrationResult.AlreadyRegistered(existing.get());
    }
    Set<Integer> busy = currentGuest.stream().map(Stay::roomNumber).collect(Collectors.toSet());
    return rooms.findAll().stream()
        .filter(r -> !busy.contains(r.number()))
        .findFirst()
        .<RegistrationResult>map(
            r ->
                new RegistrationResult.Registered(
                    r,
                    stays.save(new Stay(0, r.number(), guest, r.price(), clock.instant(), null))))
        .orElseGet(RegistrationResult.NoFreeRooms::new);
  }
}
