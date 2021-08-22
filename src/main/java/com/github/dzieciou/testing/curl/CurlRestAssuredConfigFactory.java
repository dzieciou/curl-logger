package com.github.dzieciou.testing.curl;


import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.AbstractHttpClient;

/**
 * Creates `RestAssuredConfig` objects that configure REST-assured to log each HTTP request as CURL
 * command.
 */
public class CurlRestAssuredConfigFactory {

  /**
   * Creates a REST-assured configuration to generate curl command using default options and
   * handlers.
   *
   * @return new configuration.
   */
  public static RestAssuredConfig createConfig() {
    return createConfig(getDefaultOptions());
  }

  /**
   * Creates a REST-assured configuration to generate curl command using custom options and default
   * handlers.
   *
   * @param options options defining curl generation
   * @return new configuration.
   */
  public static RestAssuredConfig createConfig(Options options) {
    return updateConfig(RestAssuredConfig.config(), options);
  }

  /**
   * Creates a REST-assured configuration to generate curl command using custom options and
   * handlers.
   *
   * @param options options defining curl generation
   * @param handlers handlers that can log or process in any other way the generated curl command.
   * @return new configuration.
   */
  public static RestAssuredConfig createConfig(Options options, List<CurlHandler> handlers) {
    return updateConfig(RestAssuredConfig.config(), options, handlers);
  }

  /**
   * Creates a REST-assured configuration to generate curl command using default options and custom
   * handlers.
   *
   * @param handlers handlers that can log or process in any other way the generated curl command.
   * @return new configuration.
   */
  public static RestAssuredConfig createConfig(List<CurlHandler> handlers) {
    return updateConfig(RestAssuredConfig.config(), handlers);
  }

  /**
   * Updates a given REST-assured configuration to generate curl command using default options and
   * handlers.
   *
   * @param config an original configuration to update
   * @return updated configuration; note original configuration remain unchanged.
   */
  public static RestAssuredConfig updateConfig(RestAssuredConfig config) {
    return updateConfig(config, getDefaultOptions(), getDefaultHandlers());
  }

  /**
   * Updates a given REST-assured configuration to generate curl command using custom options and
   * default handlers.
   *
   * @param config an original configuration to update
   * @param options options defining curl generation
   * @return updated configuration; note original configuration remain unchanged.
   */
  public static RestAssuredConfig updateConfig(RestAssuredConfig config, Options options) {
    return updateConfig(config, options, getDefaultHandlers());
  }

  /**
   * Updates a given REST-assured configuration to generate curl command using default options and
   * custom handlers.
   *
   * @param config an original configuration to update
   * @param handlers handlers that can log or process in any other way the generated curl command.
   * @return updated configuration; note original configuration remain unchanged.
   */
  public static RestAssuredConfig updateConfig(RestAssuredConfig config,
      List<CurlHandler> handlers) {
    return updateConfig(config, getDefaultOptions(), handlers);
  }

  /**
   * Updates a given REST-assured configuration to generate curl command using custom options.
   *
   * @param config an original configuration to update
   * @param options options defining curl generation
   * @param handlers handlers that can log or process in any other way the generated curl command.
   * @return updated configuration; note original configuration remain unchanged.
   */
  public static RestAssuredConfig updateConfig(RestAssuredConfig config, Options options,
      List<CurlHandler> handlers) {
    HttpClientConfig.HttpClientFactory originalFactory = getHttpClientFactory(config);
    CurlGeneratingInterceptor interceptor = new CurlGeneratingInterceptor(options,
        handlers);
    return config
        .httpClient(config.getHttpClientConfig()
            .dontReuseHttpClientInstance()
            .httpClientFactory(new MyHttpClientFactory(originalFactory, interceptor)));
  }

  private static Options getDefaultOptions() {
    return Options.builder()
        .dontLogStacktrace()
        .printSingleliner()
        .useShortForm()
        .escapeNonAscii()
        .build();
  }

  private static List<CurlHandler> getDefaultHandlers() {
    return Collections.singletonList(new CurlLogger());
  }

  private static HttpClientConfig.HttpClientFactory getHttpClientFactory(RestAssuredConfig config) {
    try {
      Field f = HttpClientConfig.class.getDeclaredField("httpClientFactory");
      f.setAccessible(true);
      HttpClientConfig httpClientConfig = config.getHttpClientConfig();
      HttpClientConfig.HttpClientFactory httpClientFactory = (HttpClientConfig.HttpClientFactory) f
          .get(httpClientConfig);
      f.setAccessible(false);
      return httpClientFactory;
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private static class MyHttpClientFactory implements HttpClientConfig.HttpClientFactory {

    private final HttpClientConfig.HttpClientFactory wrappedFactory;
    private final CurlGeneratingInterceptor curlGeneratingInterceptor;

    public MyHttpClientFactory(HttpClientConfig.HttpClientFactory wrappedFactory,
        CurlGeneratingInterceptor curlGeneratingInterceptor) {
      this.wrappedFactory = wrappedFactory;
      this.curlGeneratingInterceptor = curlGeneratingInterceptor;
    }

    @Override
    @SuppressWarnings("deprecation")
    public HttpClient createHttpClient() {
      final AbstractHttpClient client = (AbstractHttpClient) wrappedFactory.createHttpClient();
      client.addRequestInterceptor(curlGeneratingInterceptor);
      return client;
    }
  }


}
