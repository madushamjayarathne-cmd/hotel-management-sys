package com.hotel.testing;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

/** Runs every public static no-arg method whose name starts with "test". Exit code 1 on failure. */
public class AllTests {
  private static final List<Class<?>> SUITES =
      List.of(
          com.hotel.config.HotelConfigTest.class,
          com.hotel.model.GuestTest.class,
          com.hotel.model.RoomTest.class,
          com.hotel.model.StayTest.class,
          com.hotel.repository.RoomRepositoryTest.class,
          com.hotel.repository.StayRepositoryTest.class,
          com.hotel.service.RegistrationTest.class,
          com.hotel.service.CheckOutTest.class,
          com.hotel.service.QueriesTest.class,
          com.hotel.ui.ConsoleMenuTest.class);

  public static void main(String[] args) throws Exception {
    int passed = 0;
    int failed = 0;
    for (Class<?> suite : SUITES) {
      for (Method m : suite.getDeclaredMethods()) {
        if (!m.getName().startsWith("test") || !Modifier.isStatic(m.getModifiers())) {
          continue;
        }
        String name = suite.getSimpleName() + "." + m.getName();
        try {
          m.invoke(null);
          passed++;
          System.out.println("PASS " + name);
        } catch (Exception e) {
          failed++;
          Throwable cause = e.getCause() != null ? e.getCause() : e;
          System.out.println("FAIL " + name + " : " + cause.getMessage());
        }
      }
    }
    System.out.printf("%n%d passed, %d failed%n", passed, failed);
    System.exit(failed == 0 ? 0 : 1);
  }
}
