package com.github.dzieciou.testing.curl;

interface CurlHandler {

  /**
   * Handle generated curl expression.
   *
   * @param curl curl expression to handle.
   * @param options options used to generate curl.
   */
  void handle(String curl, Options options);

}
