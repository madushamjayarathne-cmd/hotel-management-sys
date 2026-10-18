package com.hotel.repository;

import com.hotel.model.BusinessRoom;
import com.hotel.model.Room;
import com.hotel.model.StandardRoom;
import com.hotel.repository.memory.InMemoryRoomRepository;
import com.hotel.testing.Check;

import java.math.BigDecimal;
import java.util.List;

public class RoomRepositoryTest {
  private static InMemoryRoomRepository repo(int n) {
    return new InMemoryRoomRepository(n, new BigDecimal("20.00"), new BigDecimal("1.5"));
  }

  public static void testHalfStandardHalfBusiness() {
    List<Room> rooms = repo(6).findAll();
    Check.equal(6, rooms.size(), "count");
    for (int i = 0; i < 6; i++) {
      Room r = rooms.get(i);
      Check.equal(i + 1, r.number(), "numbered 1..6");
      boolean business = r instanceof BusinessRoom;
      Check.equal(i >= 3, business, "room " + r.number() + " class");
    }
    Check.equal(new BigDecimal("20.00"), rooms.get(0).price(), "standard price");
    Check.equal(new BigDecimal("30.00"), rooms.get(5).price(), "business price");
  }

  public static void testRoomCountIsConfigurable() {
    Check.equal(20, repo(20).findAll().size(), "20 rooms");
    long standard = repo(20).findAll().stream().filter(r -> !(r instanceof BusinessRoom)).count();
    Check.equal(10L, standard, "10 standard");
    Check.isTrue(repo(2).findAll().getFirst() instanceof StandardRoom, "2 rooms: first standard");
  }

  public static void testOddOrNonPositiveCountRejected() {
    for (int bad : new int[] {0, -2, 5, 7}) {
      Check.throwsException(IllegalArgumentException.class, () -> repo(bad), "count " + bad);
    }
  }

  public static void testFindByNumber() {
    var repo = repo(6);
    Check.equal(4, repo.findByNumber(4).orElseThrow().number(), "found");
    Check.isTrue(repo.findByNumber(0).isEmpty(), "0 not found");
    Check.isTrue(repo.findByNumber(7).isEmpty(), "7 not found");
  }
}
