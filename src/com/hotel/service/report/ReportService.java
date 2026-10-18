package com.hotel.service.report;

import com.hotel.model.Stay;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.StayRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/** Room occupancy report. */
public class ReportService {
  private final RoomRepository rooms;
  private final StayRepository stays;
  private final Clock clock;

  public ReportService(RoomRepository rooms, StayRepository stays, Clock clock) {
    this.rooms = rooms;
    this.stays = stays;
    this.clock = clock;
  }

  public List<RoomReport> report() {
    Instant now = clock.instant();
    return rooms.findAll().stream()
        .map(
            r -> {
              List<Stay> list = stays.findByRoom(r.number());
              BigDecimal profit =
                  list.stream()
                      .map(st -> st.total(st.active() ? now : st.checkOut()))
                      .reduce(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), BigDecimal::add);
              return new RoomReport(r, list.size(), profit);
            })
        .sorted(
            Comparator.comparingInt(RoomReport::timesOccupied)
                .reversed()
                .thenComparingInt(rr -> rr.room().number()))
        .toList();
  }
}
