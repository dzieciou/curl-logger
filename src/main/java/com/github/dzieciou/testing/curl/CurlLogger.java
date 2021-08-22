package com.github.dzieciou.testing.curl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CurlLogger implements CurlHandler {

  private static final Logger log = LoggerFactory.getLogger("curl");

  @Override
  public void handle(String message, Options options) {
    switch (options.logLevel()) {
      case DEBUG:
        log.debug(message);
        break;
      case ERROR:
        log.error(message);
        break;
      case INFO:
        log.info(message);
        break;
      case TRACE:
        log.trace(message);
        break;
      case WARN:
        log.warn(message);
        break;
      default:
        throw new IllegalArgumentException("Unknown log level: " + options.logLevel());
    }
  }
}
