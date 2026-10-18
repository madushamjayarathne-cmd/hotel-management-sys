package com.hotel.service.registration;

import com.hotel.model.Room;
import com.hotel.model.Stay;

public sealed interface RegistrationResult {
  record Registered(Room room, Stay stay) implements RegistrationResult {}

  record AlreadyRegistered(Stay existing) implements RegistrationResult {}

  record NoFreeRooms() implements RegistrationResult {}

  record InvalidGuest(String reason) implements RegistrationResult {}
}
