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
import org.mockserver.client.MockServerClient;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uk.org.lidalia.slf4jext.Level;


public class CurlGeneratingInterceptorTest {

  private static final int MOCK_PORT = 9999;
  private static final String MOCK_HOST = "localhost";
  private static final String MOCK_BASE_URI = "http://" + MOCK_HOST;
  private MockServerClient mockServer;
  private TestLogger log;

  private static RestAssuredConfig getRestAssuredConfig(
      CurlGeneratingInterceptor curlGeneratingInterceptor) {
    return config()
        .httpClient(httpClientConfig()
            .reuseHttpClientInstance()
            .httpClientFactory(new MyHttpClientFactory(curlGeneratingInterceptor)));
  }

  @BeforeClass
  public void setupMock() {
    mockServer = startClientAndServer(MOCK_PORT);
    mockServer.when(request()).respond(response());
  }

  @Test
  public void shouldLogDebugMessageWithCurlCommand() {

    // given
    log = TestLoggerFactory.getTestLogger("curl");
    log.clearAll();
    Options OPTIONS = Options.builder().dontLogStacktrace().build();
    List<CurlHandler> handlers = Collections.singletonList(new CurlLogger());
    RestAssuredConfig restAssuredConfig = getRestAssuredConfig(
        new CurlGeneratingInterceptor(OPTIONS, handlers));

    // when
    //@formatter:off
    given()
        .redirects().follow(false)
        .baseUri(MOCK_BASE_URI)
        .port(MOCK_PORT)
        .config(restAssuredConfig)
        .when()
        .get("/")
        .then()
        .statusCode(200);
    //@formatter:on

    // then
    assertThat(log.getLoggingEvents().size(), is(1));
    LoggingEvent firstEvent = log.getLoggingEvents().get(0);
    assertThat(firstEvent.getLevel(), is(Level.DEBUG));
    assertThat(firstEvent.getMessage(), startsWith("curl"));
  }

  @Test
  public void shouldLogStacktraceWhenEnabled() {

    // given
    log = TestLoggerFactory.getTestLogger("curl");
    log.clearAll();
    Options options = Options.builder().logStacktrace().build();
    List<CurlHandler> handlers = Collections.singletonList(new CurlLogger());
    RestAssuredConfig restAssuredConfig = getRestAssuredConfig(
        new CurlGeneratingInterceptor(options, handlers));

    // when
    //@formatter:off
    given()
        .redirects().follow(false)
        .baseUri(MOCK_BASE_URI)
        .port(MOCK_PORT)
        .config(restAssuredConfig)
        .when()
        .get("/shouldLogStacktraceWhenEnabled")
        .then()
        .statusCode(200);
    //@formatter:on

    // then
    assertThat(log.getAllLoggingEvents().size(), is(1));
    LoggingEvent firstEvent = log.getLoggingEvents().get(0);
    assertThat(firstEvent.getLevel(), is(Level.DEBUG));
    assertThat(firstEvent.getMessage(), both(startsWith("curl")).and(containsString("generated"))
        .and(containsString(("java.lang.Thread.getStackTrace"))));
  }

  @Test
  public void testCustomHandler() {
    // given
    Options options = Options.builder().logStacktrace().build();
    final List<String> curls = new ArrayList<>();
    CurlHandler handler = (curl, options1) -> curls.add(curl);
    List<CurlHandler> handlers = Collections.singletonList(handler);
    RestAssuredConfig restAssuredConfig = getRestAssuredConfig(
        new CurlGeneratingInterceptor(options, handlers));

    // when
    //@formatter:off
    given()
        .redirects().follow(false)
        .baseUri(MOCK_BASE_URI)
        .port(MOCK_PORT)
        .config(restAssuredConfig)
        .when()
        .get("/shouldLogStacktraceWhenEnabled")
        .then()
        .statusCode(200);
    //@formatter:on

    // then
    assertThat(handlers.size(), is(1));
    assertThat(curls.get(0), is(startsWith("curl")));
  }

  @AfterMethod
  public void clearLoggers() {
    log.clearAll();
    TestLoggerFactory.clear();
  }

  @AfterClass
  public void stopMockServer() {
    mockServer.stop();
  }

  private static class MyHttpClientFactory implements HttpClientConfig.HttpClientFactory {

    private final CurlGeneratingInterceptor curlGeneratingInterceptor;

    public MyHttpClientFactory(CurlGeneratingInterceptor curlGeneratingInterceptor) {
      this.curlGeneratingInterceptor = curlGeneratingInterceptor;
    }

    @Override
    public HttpClient createHttpClient() {
      @SuppressWarnings("deprecation") AbstractHttpClient client = new DefaultHttpClient();
      client.addRequestInterceptor(curlGeneratingInterceptor);
      return client;
    }
  }
}
