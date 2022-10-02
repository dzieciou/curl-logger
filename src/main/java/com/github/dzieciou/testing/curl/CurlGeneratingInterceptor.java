package com.github.dzieciou.testing.curl;

import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/** Logs each HTTP request as CURL command in "curl" log. */
public class CurlGeneratingInterceptor implements HttpRequestInterceptor {

  private static final Logger log = LoggerFactory.getLogger("curl");
  private final Options options;

  private final Http2Curl http2Curl;

  private final List<CurlHandler> handlers;

  public CurlGeneratingInterceptor(Options options, List<CurlHandler> handlers) {
    if (handlers.isEmpty()) {
      throw new IllegalArgumentException("Missing handlers, at least one should be given");
    }
    this.options = options;
    this.handlers = new ArrayList<>(handlers);
    http2Curl = new Http2Curl(options);
  }

  private static void printStacktrace(StringBuffer sb) {
    StackTraceElement[] trace = Thread.currentThread().getStackTrace();
    for (StackTraceElement traceElement : trace) {
      sb.append("\tat ").append(traceElement).append(System.lineSeparator());
    }
  }

  @Override
  public void process(HttpRequest request, HttpContext context) {
    try {
      String curl = http2Curl.generateCurl(request);
      StringBuffer message = new StringBuffer(curl);
      if (options.canLogStacktrace()) {
        message.append(String.format("%n\tgenerated%n"));
        printStacktrace(message);
      }
      String finalMessage = message.toString();
      this.handlers.forEach(h -> h.handle(finalMessage, this.options));
    } catch (Exception e) {
      log.warn("Failed to generate CURL command for HTTP request", e);
    }
  }
}
