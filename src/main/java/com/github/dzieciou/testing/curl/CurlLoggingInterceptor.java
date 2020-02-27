package com.github.dzieciou.testing.curl;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Logs each HTTP request as CURL command in "curl" log.
 */
public class CurlLoggingInterceptor implements HttpRequestInterceptor {

  private static final Logger log = LoggerFactory.getLogger("curl");

  private final Options options;

  private final Http2Curl http2Curl;

  public CurlLoggingInterceptor(Options options) {
    this.options = options;
    http2Curl = new Http2Curl(options);
  }

  private static void printStacktrace(StringBuffer sb) {
    StackTraceElement[] trace = Thread.currentThread().getStackTrace();
    for (StackTraceElement traceElement : trace) {
      sb.append("\tat ").append(traceElement).append(System.lineSeparator());
    }
  }

  @Override
  public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
    try {
      String curl = http2Curl.generateCurl(request);
      StringBuffer message = new StringBuffer(curl);
      if (options.canLogStacktrace()) {
        message.append(String.format("%n\tgenerated%n"));
        printStacktrace(message);
      }
      if (options.getLogLevel() != null) {
        switch (options.getLogLevel()) {
          case DEBUG:
            log.debug(message.toString());
            break;
          case ERROR:
            log.error(message.toString());
            break;
          case INFO:
            log.info(message.toString());
            break;
          case TRACE:
            log.trace(message.toString());
            break;
          case WARN:
            log.warn(message.toString());
            break;
          default:
            throw new IllegalStateException("Unknown log level: " + options.getLogLevel());
        }
      }
    } catch (Exception e) {
      log.warn("Failed to generate CURL command for HTTP request", e);
    }
  }


}
