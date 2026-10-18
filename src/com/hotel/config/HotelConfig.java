package com.hotel.config;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

public final class HotelConfig {

  private static final Set<String> SUPPORTED_ENVIRONMENTS = Set.of("dev", "test", "prod");

  private static final Properties PROPERTIES = new Properties();

  private static final String ACTIVE_ENV;

  static {
    String environment = System.getProperty("app.env");

    if (environment == null || environment.isBlank()) {
      environment = System.getenv("APP_ENV");
    }

    if (environment == null || environment.isBlank()) {
      environment = "dev";
    }

    ACTIVE_ENV = environment.trim().toLowerCase(Locale.ROOT);

    if (!SUPPORTED_ENVIRONMENTS.contains(ACTIVE_ENV)) {
      throw new IllegalStateException("Unsupported environment: " + ACTIVE_ENV);
    }

    loadProperties("application.properties");
    loadProperties("application-" + ACTIVE_ENV + ".properties");

    // Fail fast if configuration is invalid.
    validateConfiguration();
  }

  private HotelConfig() {
    // Utility class: prevent instantiation.
  }

  private static void loadProperties(String fileName) {
    try (InputStream input = HotelConfig.class.getClassLoader().getResourceAsStream(fileName)) {

      if (input == null) {
        throw new IllegalStateException("Configuration file not found: " + fileName);
      }

      PROPERTIES.load(input);

    } catch (IOException e) {
      throw new IllegalStateException("Failed to load configuration: " + fileName, e);
    }
  }

  private static String getRequiredProperty(String key) {
    String value = PROPERTIES.getProperty(key);

    if (value == null || value.isBlank()) {
      throw new IllegalStateException("Missing required property: " + key);
    }

    return value.trim();
  }

  private static int getIntProperty(String key) {
    try {
      return Integer.parseInt(getRequiredProperty(key));
    } catch (NumberFormatException e) {
      throw new IllegalStateException("Invalid integer property: " + key, e);
    }
  }

  private static BigDecimal getDecimalProperty(String key) {
    try {
      return new BigDecimal(getRequiredProperty(key));
    } catch (NumberFormatException e) {
      throw new IllegalStateException("Invalid decimal property: " + key, e);
    }
  }

  private static void validateConfiguration() {
    int rooms = getTotalRooms();

    if (rooms <= 0 || rooms % 2 != 0) {
      throw new IllegalStateException("hotel.total.rooms must be positive and even.");
    }

    if (getStandardPrice().signum() <= 0) {
      throw new IllegalStateException("hotel.standard.price must be positive.");
    }

    if (getBusinessMultiplier().compareTo(BigDecimal.ONE) <= 0) {
      throw new IllegalStateException("hotel.business.multiplier must be greater than 1.");
    }
  }

  public static int getTotalRooms() {
    return getIntProperty("hotel.total.rooms");
  }

  public static BigDecimal getStandardPrice() {
    return getDecimalProperty("hotel.standard.price");
  }

  public static BigDecimal getBusinessMultiplier() {
    return getDecimalProperty("hotel.business.multiplier");
  }
}
