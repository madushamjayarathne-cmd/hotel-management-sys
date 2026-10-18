package com.hotel.repository.memory;

import com.hotel.model.Stay;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** In-memory repository that is saved to a file after every change and reloaded on start. */
public class FileBackedStayRepository extends InMemoryStayRepository {
  private final Path file;

  public FileBackedStayRepository(Path file) {
    super(load(file));
    this.file = file;
  }

  @Override
  public synchronized Stay save(Stay stay) {
    Stay saved = super.save(stay);
    try (var out = new ObjectOutputStream(Files.newOutputStream(file))) {
      out.writeObject(new ArrayList<>(snapshot()));
    } catch (IOException e) {
      throw new UncheckedIOException("Could not save data to " + file, e);
    }
    return saved;
  }

  private static List<Stay> load(Path file) {
    if (!Files.exists(file)) {
      return List.of();
    }
    try (var in = new ObjectInputStream(Files.newInputStream(file))) {
      if (in.readObject() instanceof List<?> list) {
        return list.stream().map(Stay.class::cast).toList();
      }
      return List.of();
    } catch (IOException | ClassNotFoundException e) {
      throw new IllegalStateException(
          "Could not read saved data " + file + "; delete it to start fresh", e);
    }
  }
}
