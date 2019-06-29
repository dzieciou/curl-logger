package com.github.dzieciou.testing.curl;


import static io.restassured.RestAssured.config;
import static io.restassured.RestAssured.given;
import static io.restassured.config.HttpClientConfig.httpClientConfig;
import static io.restassured.config.MultiPartConfig.multiPartConfig;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import io.restassured.http.Cookies;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.mockserver.client.MockServerClient;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@SuppressWarnings("unchecked")
public class UsingWithRestAssuredTest {

  private static final int MOCK_PORT = 9999;
  private static final String MOCK_HOST = "localhost";
  private static final String MOCK_BASE_URI = "http://" + MOCK_HOST;

  private MockServerClient mockServer;
  private TemporaryFolder tempFolder;

  private RestAssuredConfig getRestAssuredConfig(Consumer<String> curlConsumer) {
    return config()
        .httpClient(httpClientConfig()
            .reuseHttpClientInstance().httpClientFactory(new MyHttpClientFactory(curlConsumer)));
  }

  @BeforeClass
  public void setupMock() throws IOException {
    tempFolder = new TemporaryFolder();
    mockServer = startClientAndServer(MOCK_PORT);
    mockServer.when(request()).respond(response());
  }

  @Test(groups = "end-to-end-samples")
  public void cookiesTest() {

    Consumer<String> curlConsumer = mock(Consumer.class);

    //@formatter:off
    given()
        .redirects().follow(false)
        .baseUri(MOCK_BASE_URI)
        .port(MOCK_PORT)
        .cookie("token", "tokenValue")
        .cookie("context", "contextValue")
        .config(config()
            .httpClient(httpClientConfig()
                .reuseHttpClientInstance()
                .httpClientFactory(new MyHttpClientFactory(curlConsumer))))
        .when()
        .get("/access")
        .then()
        .statusCode(200);
    //@formatter:on

    verify(curlConsumer).accept("curl 'http://localhost:" + MOCK_PORT
        + "/access' -b 'token=tokenValue; context=contextValue' -H 'Accept: */*' --compressed -k -v");
  }

  @Test(groups = "end-to-end-samples")
  public void cookieWithSpecialCharactersTest() {

    Consumer<String> curlConsumer = mock(Consumer.class);

    //@formatter:off
    given()
        .redirects().follow(false)
        .baseUri(MOCK_BASE_URI)
        .port(MOCK_PORT)
        .cookie("token1",
            "1-XQLTiKxwRNyUpJYkr+IV2g==-+nLy/6GiMDj7SW/jN107UGmpf4hsM7IXsXdN9z/+7dyljV5N+0Pqpg/da0XIGOgSt2mMIIStakcjGyPlEq30Wx2gvYmVadkmH7gmcSGcaBupjlcKM2Fio96AbzJVjxUUsE5jvjBI8YlyX8fMiesQ8Gbt8XhEGbJKJe4/ogMDn7Qv687DQraxGewISOu5VIQuhgztTDqa2OUCgObG94wtAo3lSo+7HSbxcbM0LNKbbqA=-5GVOIPO4SZ7m8E0DtLS1E76h0LOmzWN00iiIeWZz360=")
        .cookie("token2",
            "2-XQLTiKxwRNyUpJYkr+IV2g==-+nLy/6GiMDj7SW/jN107UGmpf4hsM7IXsXdN9z/+7dyljV5N+0Pqpg/da0XIGOgSt2mMIIStakcjGyPlEq30Wx2gvYmVadkmH7gmcSGcaBupjlcKM2Fio96AbzJVjxUUsE5jvjBI8YlyX8fMiesQ8Gbt8XhEGbJKJe4/ogMDn7Qv687DQraxGewISOu5VIQuhgztTDqa2OUCgObG94wtAo3lSo+7HSbxcbM0LNKbbqA=-5GVOIPO4SZ7m8E0DtLS1E76h0LOmzWN00iiIeWZz360=")
        .config(config()
            .httpClient(httpClientConfig()
                .reuseHttpClientInstance()
                .httpClientFactory(new MyHttpClientFactory(curlConsumer))))
        .when()
        .get("/access")
        .then()
        .statusCode(200);
    //@formatter:on

    verify(curlConsumer).accept("curl 'http://localhost:" + MOCK_PORT + "/access' " +
        "-b '"
        + "token1=1-XQLTiKxwRNyUpJYkr+IV2g==-+nLy/6GiMDj7SW/jN107UGmpf4hsM7IXsXdN9z/+7dyljV5N+0Pqpg/da0XIGOgSt2mMIIStakcjGyPlEq30Wx2gvYmVadkmH7gmcSGcaBupjlcKM2Fio96AbzJVjxUUsE5jvjBI8YlyX8fMiesQ8Gbt8XhEGbJKJe4/ogMDn7Qv687DQraxGewISOu5VIQuhgztTDqa2OUCgObG94wtAo3lSo+7HSbxcbM0LNKbbqA=-5GVOIPO4SZ7m8E0DtLS1E76h0LOmzWN00iiIeWZz360=; "
        + "token2=2-XQLTiKxwRNyUpJYkr+IV2g==-+nLy/6GiMDj7SW/jN107UGmpf4hsM7IXsXdN9z/+7dyljV5N+0Pqpg/da0XIGOgSt2mMIIStakcjGyPlEq30Wx2gvYmVadkmH7gmcSGcaBupjlcKM2Fio96AbzJVjxUUsE5jvjBI8YlyX8fMiesQ8Gbt8XhEGbJKJe4/ogMDn7Qv687DQraxGewISOu5VIQuhgztTDqa2OUCgObG94wtAo3lSo+7HSbxcbM0LNKbbqA=-5GVOIPO4SZ7m8E0DtLS1E76h0LOmzWN00iiIeWZz360="
        + "' " +
        "-H 'Accept: */*' --compressed -k -v");
  }

  @Test(groups = "end-to-end-samples")
  public void customizedCookie() {

    Consumer<String> curlConsumer = mock(Consumer.class);

    List<Cookie> cookies = new ArrayList<>();
    cookies.add(
        new Cookie.Builder("token", "tokenValue").setDomain("testing.com").setPath("/access")
            .build());

    //@formatter:off
    given()

        .redirects().follow(false)
        .baseUri(MOCK_BASE_URI)
        .port(MOCK_PORT)
        .cookies(new Cookies(cookies))
        .config(config()
            .httpClient(httpClientConfig()
                .reuseHttpClientInstance()
                .httpClientFactory(new MyHttpClientFactory(curlConsumer))))
        .when()
        .get("/access")
        .then()
        .statusCode(200);
    //@formatter:on

    verify(curlConsumer).accept("curl 'http://localhost:" + MOCK_PORT
        + "/access' -b 'token=tokenValue' -H 'Accept: */*' --compressed -k -v");
  }

  @Test(groups = "end-to-end-samples")
  public void basicIntegrationTest() {

    Consumer<String> curlConsumer = mock(Consumer.class);

    //@formatter:off
    given()
        .redirects().follow(false)
        .baseUri(MOCK_BASE_URI)
        .port(MOCK_PORT)
        .config(getRestAssuredConfig(curlConsumer))
        .when()
        .get("/")
        .then()
        .statusCode(200);
    //@formatter:on

    verify(curlConsumer).accept(
        "curl 'http://localhost:" + MOCK_PORT
            + "/' -H 'Accept: */*' --compressed -k -v");
  }

  @Test(groups = "end-to-end-samples")
  public void shouldPrintPostRequestWithMultipartDataProperly() {

    Consumer<String> curlConsumer = mock(Consumer.class);

    //@formatter:off
    given()
        .baseUri(MOCK_BASE_URI)
        .port(MOCK_PORT)
        .config(getRestAssuredConfig(curlConsumer))
        .multiPart(new File("README.md"))
        .formParam("parameterX", "parameterXValue")
        .when().post("/");
    //@formatter:on

    verify(curlConsumer).accept(
        "curl 'http://localhost:" + MOCK_PORT
            + "/' -X POST -H 'Accept: */*' -F 'file=@README.md;type=application/octet-stream' -F 'parameterX=parameterXValue;type=text/plain' --compressed -k -v");

  }

  @Test(groups = "end-to-end-samples")
  public void shouldPrintBody() {

    Consumer<String> curlConsumer = mock(Consumer.class);

    //@formatter:off
    given()
        .baseUri(MOCK_BASE_URI)
        .port(MOCK_PORT)
        .config(getRestAssuredConfig(curlConsumer))
        .contentType(ContentType.JSON)
        .body("name=Administração")
        .when().post("/");
    //@formatter:on

    verify(curlConsumer).accept(
        "curl 'http://localhost:" + MOCK_PORT
            + "/' -H 'Accept: */*' -H 'Content-Type: application/json; charset=UTF-8' --data-binary 'name=Administração' --compressed -k -v");
  }



  @Test(groups = "end-to-end-samples")
  public void shouldPrintBodyWithEncoding() {

    Consumer<String> curlConsumer = mock(Consumer.class);

    //@formatter:off
    given()
        .baseUri(MOCK_BASE_URI)
        .port(MOCK_PORT)
        .config(getRestAssuredConfig(curlConsumer))
        .contentType(ContentType.JSON)
        .body("{\n'name':\"CKB2\",'salary':'123','age':'23'\n}")
        .when().post("/");
    //@formatter:on

    verify(curlConsumer).accept(
        "curl 'http://localhost:" + MOCK_PORT
            + "/' -H 'Accept: */*' -H 'Content-Type: application/json; charset=UTF-8' --data-binary "
            + "$'{\\n\\'name\\':\"CKB2\",\\'salary\\':\\'123\\',\\'age\\':\\'23\\'\\n}' --compressed -k -v");
  }

  @Test(groups = "end-to-end-samples")
  public void shouldPrintMultipartWithContentTypesForTypes() {

    Consumer<String> curlConsumer = mock(Consumer.class);

    //@formatter:off
    given()
        .baseUri(MOCK_BASE_URI)
        .port(MOCK_PORT)
        .config(getRestAssuredConfig(curlConsumer))
        .multiPart("message", "{content:\"interesting\"}", "application/json")
        .when().post("/");
    //@formatter:on

    verify(curlConsumer).accept(
        "curl 'http://localhost:" + MOCK_PORT
            + "/' -X POST -H 'Accept: */*' -F 'message={content:\"interesting\"};type=application/json' --compressed -k -v");

  }

  @Test
  public void shouldPrintMultipartWithMixedType() {

    Consumer<String> curlConsumer = mock(Consumer.class);

    //@formatter:off
    given()
        .baseUri(MOCK_BASE_URI)
        .port(MOCK_PORT)
        .config(getRestAssuredConfig(curlConsumer)
            .multiPartConfig(multiPartConfig().defaultSubtype("mixed")))
        .multiPart("myfile", new File("README.md"), "application/json")
        .when().post("/");
    //@formatter:on

    verify(curlConsumer).accept(
        "curl 'http://localhost:" + MOCK_PORT
            + "/' -X POST -H 'Accept: */*' -H 'Content-Type: multipart/mixed' -F 'myfile=@README.md;type=application/json' --compressed -k -v");
  }

  @Test
  public void shouldPrintFileAsBinary() throws IOException {
    Consumer<String> curlConsumer = mock(Consumer.class);

    File tempFile = tempFolder.createFile().toFile();
    FileUtils.writeStringToFile(tempFile,
        "{ 'message' : 'hello world'}", Charset.defaultCharset());

    //@formatter:off
    given()
        .baseUri(MOCK_BASE_URI)
        .port(MOCK_PORT)
        .config(getRestAssuredConfig(curlConsumer))
        .body(tempFile)
        .when().post("/");
    //@formatter:on

    verify(curlConsumer).accept(
        "curl 'http://localhost:" + MOCK_PORT
            + "/' -H 'Accept: */*' -H 'Content-Type: text/plain; charset=ISO-8859-1' --data-binary $'{ \\'message\\' : \\'hello world\\'}' --compressed -k -v");
  }

  @Test
  public void shouldPrintForm() {
    Consumer<String> curlConsumer = mock(Consumer.class);

    //@formatter:off
    given()
        .baseUri(MOCK_BASE_URI)
        .port(MOCK_PORT)
        .config(getRestAssuredConfig(curlConsumer))
        .formParam("birthyear","1905")
        .formParam("invitation","I am Daniel")
        .when().post("/");
    //@formatter:on

    verify(curlConsumer).accept(
        "curl 'http://localhost:" + MOCK_PORT
            + "/' -H 'Accept: */*' -H 'Content-Type: application/x-www-form-urlencoded; charset=ISO-8859-1' --data-binary 'birthyear=1905&invitation=I%20am%20Daniel' --compressed -k -v");
  }

  @Test(groups = "end-to-end-samples")
  public void shouldPrintPut() {

    Consumer<String> curlConsumer = mock(Consumer.class);

    //@formatter:off
    given()
        .redirects().follow(false)
        .baseUri(MOCK_BASE_URI)
        .port(MOCK_PORT)
        .config(getRestAssuredConfig(curlConsumer))
        .when()
        .put("/")
        .then()
        .statusCode(200);
    //@formatter:on

    verify(curlConsumer).accept(
        "curl 'http://localhost:" + MOCK_PORT
            + "/' -X PUT -H 'Accept: */*' -H 'Content-Length: 0' --compressed -k -v");
  }



  @AfterClass
  public void closeMock() {
    mockServer.stop();
    tempFolder.deleteAll();
  }

  private class MyHttpClientFactory implements HttpClientConfig.HttpClientFactory {

    public final Consumer<String> curlConsumer;

    private MyHttpClientFactory(Consumer<String> curlConsumer) {
      this.curlConsumer = curlConsumer;
    }

    @SuppressWarnings("deprecation")
    @Override
    public HttpClient createHttpClient() {
      AbstractHttpClient client = new DefaultHttpClient();
      client.addRequestInterceptor(new CurlTestingInterceptor(curlConsumer));
      return client;
    }
  }

  private static class CurlTestingInterceptor implements HttpRequestInterceptor {

    public final Consumer<String> curlConsumer;

    public CurlTestingInterceptor(Consumer<String> curlConsumer) {
      this.curlConsumer = curlConsumer;
    }

    @Override
    public void process(HttpRequest request, HttpContext context)
        throws HttpException, IOException {


        Options options = Options.builder()
            .printSingleliner()
            .targetPlatform(Platform.UNIX)
            .useShortForm()
            .updateCurl(curl -> curl
                .removeHeader("Host")
                .removeHeader("User-Agent")
                .removeHeader("Connection"))
            .build();

      try {
        curlConsumer.accept(new Http2Curl(options).generateCurl(request));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

    }
  }

}
