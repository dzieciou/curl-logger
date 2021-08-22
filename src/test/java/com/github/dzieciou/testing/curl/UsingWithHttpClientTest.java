package com.github.dzieciou.testing.curl;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.testng.annotations.Test;

public class UsingWithHttpClientTest {

  @Test(groups = "end-to-end-samples")
  public void testHttp() throws IOException {
    TestLoggerFactory.clearAll();
    HttpGet getRequest = new HttpGet("http://google.com");
    createHttpClient().execute(getRequest);
    assertThat(
        getAllLoggedMessages(),
        hasItem("curl 'http://google.com/' --compressed --insecure --verbose"));
  }

  @Test(groups = "end-to-end-samples")
  public void testHttps() throws IOException {
    TestLoggerFactory.clearAll();
    HttpGet getRequest = new HttpGet("https://google.com");
    createHttpClient().execute(getRequest);
    assertThat(
        getAllLoggedMessages(),
        hasItem("curl 'https://google.com/' --compressed --insecure --verbose"));
  }

  private static HttpClient createHttpClient() {
    return HttpClientBuilder.create()
        .addInterceptorFirst(
            new CurlGeneratingInterceptor(
                Options.builder()
                    .targetPlatform(Platform.UNIX) // TO ease verifying output curl
                    .build(),
                Collections.singletonList(new CurlLogger())))
        .build();
  }

  private static List<String> getAllLoggedMessages() {
    return TestLoggerFactory.getAllLoggingEvents().stream()
        .map(LoggingEvent::getMessage)
        .collect(Collectors.toList());
  }
}
