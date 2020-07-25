package com.github.dzieciou.testing.curl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import org.slf4j.event.Level;
import org.testng.annotations.Test;

public class CurlLoggerTest {

  private TestLogger log;

  @Test
  public void shouldLogAtErrorLevel() {
    // given
    log = TestLoggerFactory.getTestLogger("curl");
    log.clearAll();
    Options options = Options.builder().useLogLevel(Level.ERROR).build();
    CurlHandler handler = new CurlLogger();

    // when
    handler.handle("curl expression", options);

    // then
    assertThat(log.getAllLoggingEvents().size(), is(1));
    LoggingEvent firstEvent = log.getLoggingEvents().get(0);
    assertThat(firstEvent.getLevel().name(), is(Level.ERROR.name()));
    assertThat(firstEvent.getMessage(), startsWith("curl"));
  }
}