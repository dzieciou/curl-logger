package com.github.dzieciou.testing.curl;


import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.AbstractHttpClient;

import java.lang.reflect.Field;

import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;

/**
 * Creates `RestAssuredConfig` objects that configure REST-assured to log each HTTP request as CURL
 * command.
 */
public class CurlLoggingRestAssuredConfigFactory {

  /**
   * Creates a REST-assured configuration to generate curl command using default options.
   *
   * @return new configuration.
   */
  public static RestAssuredConfig createConfig() {
    return createConfig(getDefaultOptions());
  }

  /**
   * Creates a REST-assured configuration to generate curl command using custom options.
   *
   * @param options options defining curl generation
   * @return new configuration.
   */
  public static RestAssuredConfig createConfig(Options options) {
    return updateConfig(RestAssuredConfig.config(), options);
  }

  /**
   * Updates a given REST-assured configuration to generate curl command using default options.
   *
   * @param config an original configuration to update
   * @return updated configuration; note original configuration remain unchanged.
   */
  public static RestAssuredConfig updateConfig(RestAssuredConfig config) {
    return updateConfig(config, getDefaultOptions());
  }

  /**
   * Updates a given REST-assured configuration to generate curl command using custom options.
   *
   * @param config  an original configuration to update
   * @param options options defining curl generation
   * @return updated configuration; note original configuration remain unchanged.
   */
  public static RestAssuredConfig updateConfig(RestAssuredConfig config, Options options) {
    HttpClientConfig.HttpClientFactory originalFactory = getHttpClientFactory(config);
    return config
        .httpClient(config.getHttpClientConfig()
            .reuseHttpClientInstance()
            .httpClientFactory(new MyHttpClientFactory(originalFactory, new CurlLoggingInterceptor(options))));
  }

  private static Options getDefaultOptions() {
    return Options.builder()
        .dontLogStacktrace()
        .printSingleliner()
        .useShortForm()
        .escapeNonAscii()
        .build();
  }

  private static HttpClientConfig.HttpClientFactory getHttpClientFactory(RestAssuredConfig config) {
    try {
      Field f = HttpClientConfig.class.getDeclaredField("httpClientFactory");
      f.setAccessible(true);
      HttpClientConfig httpClientConfig = config.getHttpClientConfig();
      HttpClientConfig.HttpClientFactory httpClientFactory = (HttpClientConfig.HttpClientFactory) f.get(httpClientConfig);
      f.setAccessible(false);
      return httpClientFactory;
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private static class MyHttpClientFactory implements HttpClientConfig.HttpClientFactory {

    private final HttpClientConfig.HttpClientFactory wrappedFactory;
    private final CurlLoggingInterceptor curlLoggingInterceptor;

    public MyHttpClientFactory(HttpClientConfig.HttpClientFactory wrappedFactory,
                               CurlLoggingInterceptor curlLoggingInterceptor) {
      this.wrappedFactory = wrappedFactory;
      this.curlLoggingInterceptor = curlLoggingInterceptor;
    }

    @Override
    @SuppressWarnings("deprecation")
    public HttpClient createHttpClient() {
      final AbstractHttpClient client = (AbstractHttpClient) wrappedFactory.createHttpClient();
      client.addRequestInterceptor(curlLoggingInterceptor);
      return client;
    }
  }


}
