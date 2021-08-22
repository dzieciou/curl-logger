package com.github.dzieciou.testing.curl;

public enum Platform {
  RECOGNIZE_AUTOMATICALLY(
      System.getProperty("os.name") != null && System.getProperty("os.name").startsWith("Windows"),
      System.lineSeparator()),

  WINDOWS(true, "\r\n"),

  UNIX(false, "\n");

  private final boolean osWindows;
  private final String lineSeparator;

  Platform(boolean osWindows, String lineSeparator) {
    this.osWindows = osWindows;
    this.lineSeparator = lineSeparator;
  }

  public boolean isOsWindows() {
    return osWindows;
  }

  public String lineSeparator() {
    return lineSeparator;
  }
}
