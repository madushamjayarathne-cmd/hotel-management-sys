package com.hotel;

import com.hotel.facade.HotelFacade;
import com.hotel.facade.impl.HotelFacadeImpl;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.StayRepository;
import com.hotel.service.registration.RoomAllocationStrategy;
import com.hotel.service.registration.impl.FirstFreeRoomStrategy;
import com.hotel.service.registration.impl.RegistrationServiceImpl;
import com.hotel.service.checkout.impl.CheckOutServiceImpl;
import com.hotel.service.report.impl.ReportServiceImpl;
import com.hotel.service.room.impl.RoomDetailsServiceImpl;

import java.time.Clock;

public final class HotelFactory {
  private HotelFactory() {}

  public static HotelFacade create(RoomRepository rooms, StayRepository stays) {
    return create(rooms, stays, Clock.systemDefaultZone());
  }

  /** The clock is injectable for tests. */
  public static HotelFacade create(RoomRepository rooms, StayRepository stays, Clock clock) {
    return create(rooms, stays, clock, new FirstFreeRoomStrategy());
  }

  public static HotelFacade create(
      RoomRepository rooms, StayRepository stays, Clock clock, RoomAllocationStrategy strategy) {
    return new HotelFacadeImpl(
        new RegistrationServiceImpl(rooms, stays, clock, strategy),
        new CheckOutServiceImpl(rooms, stays, clock),
        new RoomDetailsServiceImpl(rooms, stays),
        new ReportServiceImpl(rooms, stays, clock));
  }
}
