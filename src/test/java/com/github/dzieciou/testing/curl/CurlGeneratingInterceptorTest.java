package com.github.dzieciou.testing.curl;

import static io.restassured.RestAssured.config;
import static io.restassured.RestAssured.given;
import static io.restassured.config.HttpClientConfig.httpClientConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import uk.org.lidalia.slf4jext.Level;

public class CurlGeneratingInterceptorTest {

  private static final int MOCK_PORT = 9999;
  private static final String MOCK_HOST = "localhost";
  private static final String MOCK_BASE_URI = "http://" + MOCK_HOST;
  private static MockServerClient mockServer;
  private static TestLogger log;

  private static RestAssuredConfig getRestAssuredConfig(
      CurlGeneratingInterceptor curlGeneratingInterceptor) {
    return config()
        .httpClient(
            httpClientConfig()
                .reuseHttpClientInstance()
                .httpClientFactory(new MyHttpClientFactory(curlGeneratingInterceptor)));
  }

  @BeforeAll
  public static void setupMock() {
    mockServer = startClientAndServer(MOCK_PORT);
    mockServer.when(request()).respond(response());
    log = TestLoggerFactory.getTestLogger("curl");
  }

  @Test
  public void shouldLogDebugMessageWithCurlCommand() {

    // given
    log.clearAll();
    Options OPTIONS = Options.builder().dontLogStacktrace().build();
    List<CurlHandler> handlers = Collections.singletonList(new CurlLogger());
    RestAssuredConfig restAssuredConfig =
        getRestAssuredConfig(new CurlGeneratingInterceptor(OPTIONS, handlers));

    // when
    given()
        .redirects()
        .follow(false)
        .baseUri(MOCK_BASE_URI)
        .port(MOCK_PORT)
        .config(restAssuredConfig)
        .when()
        .get("/")
        .then()
        .statusCode(200);

    // then
    assertThat(log.getLoggingEvents().size(), is(1));
    LoggingEvent firstEvent = log.getLoggingEvents().get(0);
    assertThat(firstEvent.getLevel(), is(Level.DEBUG));
    assertThat(firstEvent.getMessage(), startsWith("curl"));
  }

  @Test
  public void shouldLogStacktraceWhenEnabled() {

    // given
    log.clearAll();
    Options options = Options.builder().logStacktrace().build();
    List<CurlHandler> handlers = Collections.singletonList(new CurlLogger());
    RestAssuredConfig restAssuredConfig =
        getRestAssuredConfig(new CurlGeneratingInterceptor(options, handlers));

    // when
    given()
        .redirects()
        .follow(false)
        .baseUri(MOCK_BASE_URI)
        .port(MOCK_PORT)
        .config(restAssuredConfig)
        .when()
        .get("/shouldLogStacktraceWhenEnabled")
        .then()
        .statusCode(200);

    // then
    assertThat(log.getAllLoggingEvents().size(), is(1));
    LoggingEvent firstEvent = log.getLoggingEvents().get(0);
    assertThat(firstEvent.getLevel(), is(Level.DEBUG));
    assertThat(
        firstEvent.getMessage(),
        both(startsWith("curl"))
            .and(containsString("generated"))
            .and(containsString(("java.lang.Thread.getStackTrace"))));
  }

  @Test
  public void testCustomHandler() {
    // given
    Options options = Options.builder().logStacktrace().build();
    final List<String> curls = new ArrayList<>();
    CurlHandler handler = (curl, options1) -> curls.add(curl);
    List<CurlHandler> handlers = Collections.singletonList(handler);
    RestAssuredConfig restAssuredConfig =
        getRestAssuredConfig(new CurlGeneratingInterceptor(options, handlers));

    // when
    given()
        .redirects()
        .follow(false)
        .baseUri(MOCK_BASE_URI)
        .port(MOCK_PORT)
        .config(restAssuredConfig)
        .when()
        .get("/shouldLogStacktraceWhenEnabled")
        .then()
        .statusCode(200);

    // then
    assertThat(handlers.size(), is(1));
    assertThat(curls.get(0), is(startsWith("curl")));
  }

  @AfterEach
  public void clearLoggers() {
    log.clearAll();
    TestLoggerFactory.clear();
  }

  @AfterAll
  public static void stopMockServer() {
    mockServer.stop();
  }

  private static class MyHttpClientFactory implements HttpClientConfig.HttpClientFactory {

    private final CurlGeneratingInterceptor curlGeneratingInterceptor;

    public MyHttpClientFactory(CurlGeneratingInterceptor curlGeneratingInterceptor) {
      this.curlGeneratingInterceptor = curlGeneratingInterceptor;
    }

    @Override
    public HttpClient createHttpClient() {
      @SuppressWarnings("deprecation")
      AbstractHttpClient client = new DefaultHttpClient();
      client.addRequestInterceptor(curlGeneratingInterceptor);
      return client;
    }
  }
}
