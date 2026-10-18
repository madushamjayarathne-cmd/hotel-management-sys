package com.hotel.repository;

import com.hotel.model.Guest;
import com.hotel.model.Stay;
import com.hotel.repository.memory.FileBackedStayRepository;
import com.hotel.repository.memory.InMemoryStayRepository;
import com.hotel.testing.Check;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

public class StayRepositoryTest {
  private static Stay newStay(int room, String name) {
    return new Stay(0, room, new Guest(name, "Test"), new BigDecimal("20.00"), Instant.now(), null);
  }

  public static void testSaveAssignsIncreasingIds() {
    var repo = new InMemoryStayRepository(List.of());
    Check.equal(1L, repo.save(newStay(1, "Ann")).id(), "first id");
    Check.equal(2L, repo.save(newStay(2, "Bob")).id(), "second id");
  }

  public static void testSaveExistingUpdates() {
    var repo = new InMemoryStayRepository(List.of());
    Stay saved = repo.save(newStay(1, "Ann"));
    repo.save(saved.checkedOut(Instant.now()));
    Check.equal(1, repo.findByRoom(1).size(), "still one record");
    Check.isTrue(!repo.findByRoom(1).getFirst().active(), "now checked out");
  }

  public static void testFindActive() {
    var repo = new InMemoryStayRepository(List.of());
    Stay a = repo.save(newStay(1, "Ann"));
    repo.save(newStay(2, "Bob"));
    repo.save(a.checkedOut(Instant.now()));
    Check.equal(1, repo.findActive().size(), "one active");
    Check.equal(2, repo.findActive().getFirst().roomNumber(), "room 2 active");
    Check.isTrue(repo.findActiveByRoom(1).isEmpty(), "room 1 has no active stay");
    Check.isTrue(repo.findActiveByRoom(2).isPresent(), "room 2 has active stay");
  }

  public static void testFindByRoomKeepsCheckInOrder() {
    var repo = new InMemoryStayRepository(List.of());
    repo.save(newStay(1, "Ann"));
    repo.save(newStay(2, "Bob"));
    repo.save(newStay(1, "Cid"));
    var list = repo.findByRoom(1);
    Check.equal("Ann", list.get(0).guest().name(), "first");
    Check.equal("Cid", list.get(1).guest().name(), "second");
  }

  public static void testIdsContinueAfterInitialData() {
    var seed = new Stay(5, 1, new Guest("Old", "Guest"), BigDecimal.TEN, Instant.now(), null);
    var repo = new InMemoryStayRepository(List.of(seed));
    Check.equal(6L, repo.save(newStay(2, "New")).id(), "next id after 5");
  }

  public static void testFileBackedPersistsBetweenRuns() throws IOException {
    Path file = Files.createTempFile("hotel-test", ".ser");
    Files.delete(file);
    try {
      var first = new FileBackedStayRepository(file);
      Stay saved = first.save(newStay(3, "Ann"));
      first.save(newStay(4, "Bob"));
      first.save(saved.checkedOut(Instant.now()));

      var second = new FileBackedStayRepository(file);
      Check.equal(1, second.findByRoom(3).size(), "room 3 stay restored");
      Check.isTrue(!second.findByRoom(3).getFirst().active(), "check-out restored");
      Check.equal(1, second.findActive().size(), "active restored");
      Check.equal(3L, second.save(newStay(5, "Cid")).id(), "id counter restored");
    } finally {
      Files.deleteIfExists(file);
    }
  }

  public static void testFileBackedStartsEmptyWithoutFile() throws IOException {
    Path file = Files.createTempFile("hotel-test", ".ser");
    Files.delete(file);
    try {
      Check.equal(0, new FileBackedStayRepository(file).findActive().size(), "empty");
    } finally {
      Files.deleteIfExists(file);
    }
  }

  public static void testFileBackedCorruptFileFailsClearly() throws IOException {
    Path file = Files.createTempFile("hotel-test", ".ser");
    try {
      Files.writeString(file, "not a serialized list");
      Check.throwsException(
          IllegalStateException.class, () -> new FileBackedStayRepository(file), "corrupt");
    } finally {
      Files.deleteIfExists(file);
    }
  }
}
