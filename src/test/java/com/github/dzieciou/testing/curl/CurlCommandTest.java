package com.github.dzieciou.testing.curl;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import org.testng.annotations.Test;

public class CurlCommandTest {

  @Test
  public void shouldRespectTargetPlatformInMultilinePrinting() {
    CurlCommand curl = new CurlCommand()
        .setUrl("/requestPath")
        .addHeader("Host", "server.com")
        .addHeader("Other", "other");

    assertThat(curl.asString(Platform.WINDOWS, true, true, true),
        containsString("^"));

    assertThat(curl.asString(Platform.UNIX, true, true, true),
        not(containsString("^")));
  }

  @Test
  public void shouldEscapeNonAsciiCharacters() {

    CurlCommand curl = new CurlCommand()
        .setUrl("/requestPath")
        .addDataBinary("name=Administração");

    assertThat(curl.asString(Platform.UNIX, true, false, true),
        equalTo("curl '/requestPath' --data-binary $'name=Administra\\xe7\\xe3o'"));
  }

  @Test
  public void shouldNotEscapeNonAsciiCharacters() {

    CurlCommand curl = new CurlCommand()
        .setUrl("/requestPath")
        .addDataBinary("name=Administração");

    assertThat(curl.asString(Platform.UNIX, true, false, false),
        equalTo("curl '/requestPath' --data-binary 'name=Administração'"));
  }

  @Test
  public void shouldEscapeSingleQuotes() {

    CurlCommand curl = new CurlCommand()
        .setUrl("/requestPath")
        .addDataBinary("{'name':'John'}");

    assertThat(curl.asString(Platform.UNIX, true, false, true),
        equalTo("curl '/requestPath' --data-binary $'{\\'name\\':\\'John\\'}'"));
  }


  @Test
  public void shouldEscapeAtCharacter() {

    CurlCommand curl = new CurlCommand()
        .setUrl("/requestPath")
        .addDataBinary("maciek@gmail.com");

    assertThat(curl.asString(Platform.UNIX, true, false, true),
        equalTo("curl '/requestPath' --data-binary $'maciek\\x40gmail.com'"));
  }
}
