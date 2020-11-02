package com.github.dzieciou.testing.curl;

import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mockserver.client.MockServerClient;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static com.github.dzieciou.testing.curl.CommandExecutor.runCommand;
import static io.restassured.RestAssured.config;
import static io.restassured.RestAssured.given;
import static io.restassured.config.HttpClientConfig.httpClientConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class Bug {

    private MockServerClient mockServer;

    String matchingBody = "secret=P@ssword";

    @BeforeMethod
    public void setup() {
        mockServer = startClientAndServer(9999);
        mockServer.when(request().withBody(matchingBody)).respond(response());
    }

    @Test
    public void buggy() throws IOException {
        Options options = Options.builder().dontEscapeNonAscii().build();
        final List<String> curls = new ArrayList<>();
        CurlHandler handler = new CurlHandler() {
            @Override
            public void handle(String curl, Options options) {
                curls.add(curl);
            }
        };
        List<CurlHandler> handlers = Arrays.asList(handler, new CurlLogger());
        RestAssuredConfig restAssuredConfig = getRestAssuredConfig(
                new CurlGeneratingInterceptor(options, handlers));


        //@formatter:off
        given()
                .baseUri("http://localhost")
                .port(9999)
                .config(restAssuredConfig)
                .body(matchingBody)
                .when()
                .post()
                .then()
                .statusCode(200);

        //@formatter:on

        String curl = curls.get(0);
        System.out.println(curl);
        String output = runCommand(curl).stdErr;
        assertThat(output, containsString("HTTP/1.1 200 OK"));

    }

    @AfterMethod
    public void after() {
        mockServer.stop();
    }

    private static RestAssuredConfig getRestAssuredConfig(
            CurlGeneratingInterceptor curlGeneratingInterceptor) {
        return config()
                .httpClient(httpClientConfig()
                        .reuseHttpClientInstance()
                        .httpClientFactory(new MyHttpClientFactory(curlGeneratingInterceptor)));
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
