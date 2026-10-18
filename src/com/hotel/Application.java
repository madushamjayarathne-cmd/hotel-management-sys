package com.hotel;

import com.hotel.config.HotelConfig;
import com.hotel.facade.HotelFacade;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.StayRepository;
import com.hotel.repository.memory.FileBackedStayRepository;
import com.hotel.repository.memory.InMemoryRoomRepository;
import com.hotel.ui.ConsoleMenu;

import java.nio.file.Path;

public class Application {
  public static void main(String[] args) {
    RoomRepository rooms =
        new InMemoryRoomRepository(
            HotelConfig.getTotalRooms(),
            HotelConfig.getStandardPrice(),
            HotelConfig.getBusinessMultiplier());
    StayRepository stays = new FileBackedStayRepository(Path.of("hotel-data.log"));

    HotelFacade hotel = HotelFactory.create(rooms, stays);
    new ConsoleMenu(hotel).run();
  }
}
