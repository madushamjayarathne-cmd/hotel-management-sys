package com.hotel.repository.memory;

import com.hotel.model.Guest;
import com.hotel.model.Stay;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class FileBackedStayRepository extends InMemoryStayRepository {
  private final Path file;

  public FileBackedStayRepository(Path file) {
    this(file, load(file));
  }

  private FileBackedStayRepository(Path file, Loaded loaded) {
    super(loaded.stays());
    this.file = file;
    if (loaded.needsCompaction()) {
      compact();
    }
  }

  @Override
  public synchronized Stay save(Stay stay) {
    Stay saved = super.save(stay);
    try {
      Files.writeString(
          file,
          format(saved) + "\n",
          StandardCharsets.UTF_8,
          StandardOpenOption.CREATE,
          StandardOpenOption.APPEND);
    } catch (IOException e) {
      throw new UncheckedIOException("Could not save data to " + file, e);
    }
    return saved;
  }

  private void compact() {
    Path temp = file.resolveSibling(file.getFileName() + ".tmp");
    try {
      var lines = snapshot().stream().map(FileBackedStayRepository::format).toList();
      Files.write(temp, lines, StandardCharsets.UTF_8);
      Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    } catch (IOException e) {
      throw new UncheckedIOException("Could not compact data file " + file, e);
    }
  }

  private record Loaded(List<Stay> stays, boolean needsCompaction) {}

  private static Loaded load(Path file) {
    return Files.exists(file) ? replay(file) : new Loaded(List.of(), false);
  }

  private static Loaded replay(Path file) {
    try {
      String content = Files.readString(file, StandardCharsets.UTF_8);
      List<String> lines = new ArrayList<>(List.of(content.split("\n", -1)));
      // The text after the last newline is either empty or a partially written line.
      boolean partial = !lines.removeLast().isEmpty();
      var latest = new LinkedHashMap<Long, Stay>();
      for (int i = 0; i < lines.size(); i++) {
        if (lines.get(i).isBlank()) {
          continue;
        }
        try {
          Stay stay = parse(lines.get(i));
          latest.put(stay.id(), stay);
        } catch (RuntimeException e) {
          throw new IllegalStateException(
              "Could not read saved data "
                  + file
                  + " line "
                  + (i + 1)
                  + "; delete it to start fresh",
              e);
        }
      }
      boolean superseded = lines.stream().filter(l -> !l.isBlank()).count() > latest.size();
      return new Loaded(List.copyOf(latest.values()), partial || superseded);
    } catch (IOException e) {
      throw new IllegalStateException("Could not read saved data " + file, e);
    }
  }

  private static String format(Stay s) {
    return String.join(
        "|",
        Long.toString(s.id()),
        Integer.toString(s.roomNumber()),
        s.guest().name(),
        s.guest().surname(),
        s.dailyPrice().toPlainString(),
        Long.toString(s.checkIn().toEpochMilli()),
        s.checkOut() == null ? "" : Long.toString(s.checkOut().toEpochMilli()));
  }

  private static Stay parse(String line) {
    String[] f = line.split("\\|", -1);
    if (f.length != 7) {
      throw new IllegalArgumentException("expected 7 fields but found " + f.length);
    }
    return new Stay(
        Long.parseLong(f[0]),
        Integer.parseInt(f[1]),
        new Guest(f[2], f[3]),
        new BigDecimal(f[4]),
        Instant.ofEpochMilli(Long.parseLong(f[5])),
        f[6].isEmpty() ? null : Instant.ofEpochMilli(Long.parseLong(f[6])));
  }
}
