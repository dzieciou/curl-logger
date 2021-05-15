package com.github.dzieciou.testing.curl;

import java.util.Optional;
import java.util.function.Consumer;
import org.slf4j.event.Level;

public class Options {

  private boolean logStacktrace;
  private boolean printMultiliner;
  private boolean useShortForm;
  private Level logLevel = Level.DEBUG;
  private Consumer<CurlCommand> curlUpdater;
  private Platform targetPlatform = Platform.RECOGNIZE_AUTOMATICALLY;
  private boolean escapeNonAscii;
  private boolean alwaysPrintMethod;

  private Options() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public boolean canLogStacktrace() {
    return logStacktrace;
  }

  public boolean printMultiliner() {
    return printMultiliner;
  }

  public boolean escapeNonAscii() {
    return escapeNonAscii;
  }

  public boolean useShortForm() {
    return useShortForm;
  }

  public Optional<Consumer<CurlCommand>> getCurlUpdater() {
    return Optional.ofNullable(curlUpdater);
  }

  public Level logLevel() {
    return this.logLevel;
  }

  public Platform getTargetPlatform() {
    return targetPlatform;
  }

  public boolean alwaysPrintMethod() { return alwaysPrintMethod; }

  public static class Builder {

    private final Options options = new Options();

    /**
     * Configures the library to print a stacktrace where curl command has been generated.
     */
    public Builder logStacktrace() {
      options.logStacktrace = true;
      return this;
    }

    /**
     * Configures the library to not print a stacktrace where curl command has been generated.
     */
    public Builder dontLogStacktrace() {
      options.logStacktrace = false;
      return this;
    }

    /**
     * Configures the library to print a curl command in multiple lines.
     */
    public Builder printMultiliner() {
      options.printMultiliner = true;
      return this;
    }

    /**
     * Configures the library to print a curl command in a single line.
     */
    public Builder printSingleliner() {
      options.printMultiliner = false;
      return this;
    }

    /**
     * Configures the library to print short form of curl parameters.
     */
    public Builder useShortForm() {
      options.useShortForm = true;
      return this;
    }

    /**
     * Configures the library to print long form of curl parameters.
     */
    public Builder useLongForm() {
      options.useShortForm = false;
      return this;
    }

    /**
     * Configures the library to update curl command with a given {@code curlUpdater} before
     * printing.
     */
    public Builder updateCurl(Consumer<CurlCommand> curlUpdater) {
      options.curlUpdater = curlUpdater;
      return this;
    }

    /**
     * Configure the library to print curl command that will be executable on a given {@code
     * targetPlatform}.
     */
    public Builder targetPlatform(Platform targetPlatform) {
      options.targetPlatform = targetPlatform;
      return this;
    }

    /**
     * Enable escaping non ASCII characters for POSIX platforms.
     */
    public Builder escapeNonAscii() {
      options.escapeNonAscii = true;
      return this;
    }

    /**
     * Disable escaping non ASCII characters for POSIX platforms.
     */
    public Builder dontEscapeNonAscii() {
      options.escapeNonAscii = false;
      return this;
    }

    /**
     * Changes logging to a custom level.
     */
    public Builder useLogLevel(Level level) {
      options.logLevel = level;
      return this;
    }

    /**
     * Always print HTTP method, including GET method.
     */
    public Builder alwaysPrintMethod() {
      options.alwaysPrintMethod = true;
      return this;
    }

    public Options build() {
      return options;
    }

  }
}
