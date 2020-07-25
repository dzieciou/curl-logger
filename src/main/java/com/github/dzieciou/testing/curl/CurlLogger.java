package com.github.dzieciou.testing.curl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CurlLogger implements CurlHandler {

  private static final Logger log = LoggerFactory.getLogger("curl");

  @Override
  public void handle(String curl, Options options) {
    log.debug(curl);
  }
}