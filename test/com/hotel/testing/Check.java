package com.hotel.testing;

import java.util.Objects;

/** Minimal assertions; no test framework needed. */
public final class Check {
  private Check() {}

  public static void isTrue(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
  }

  public static void equal(Object expected, Object actual, String message) {
    if (!Objects.equals(expected, actual)) {
      throw new AssertionError(message + " - expected <" + expected + "> but was <" + actual + ">");
    }
  }

  public static void contains(String text, String part, String message) {
    if (text == null || !text.contains(part)) {
      throw new AssertionError(
          message + " - expected output to contain <" + part + "> but was:\n" + text);
    }
  }

  public static void throwsException(
      Class<? extends Throwable> type, Runnable code, String message) {
    try {
      code.run();
    } catch (Throwable t) {
      if (type.isInstance(t)) {
        return;
      }
      throw new AssertionError(message + " - expected " + type.getSimpleName() + " but got " + t);
    }
    throw new AssertionError(
        message + " - expected " + type.getSimpleName() + " but nothing was thrown");
  }
}
