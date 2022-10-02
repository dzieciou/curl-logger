package com.github.dzieciou.testing.curl;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CurlCommandTest {

  @Test
  public void shouldRespectTargetPlatformInMultilinePrinting() {
    CurlCommand curl =
        new CurlCommand()
            .setUrl("/requestPath")
            .addHeader("Host", "server.com")
            .addHeader("Other", "other");

    assertThat(curl.asString(Platform.WINDOWS, true, true, true), containsString("^"));

    assertThat(curl.asString(Platform.UNIX, true, true, true), not(containsString("^")));
  }

  @Test
  public void shouldEscapeNonAsciiCharactersOnUnixOnly() {

    CurlCommand curl =
        new CurlCommand().setUrl("/requestPath").addDataBinary("name=Administração" + (char) 300);

    assertThat(
        curl.asString(Platform.UNIX, true, false, true),
        equalTo("curl '/requestPath' --data-binary $'name=Administra\\xe7\\xe3o\\u012c'"));

    assertThat(
        curl.asString(Platform.WINDOWS, true, false, true),
        equalTo("curl \"/requestPath\" --data-binary \"name=Administração" + (char) 300 + "\""));
  }

  @Test
  public void shouldNotEscapeNonAsciiCharactersWithOptionDisabled() {

    CurlCommand curl = new CurlCommand().setUrl("/requestPath").addDataBinary("name=Administração");

    boolean escapeNonAscii = false;
    assertThat(
        curl.asString(Platform.UNIX, true, false, escapeNonAscii),
        equalTo("curl '/requestPath' --data-binary 'name=Administração'"));
    assertThat(
        curl.asString(Platform.WINDOWS, true, false, escapeNonAscii),
        equalTo("curl \"/requestPath\" --data-binary \"name=Administração\""));
  }

  @Test
  public void shouldEscapeSingleQuotesOnUnixOnly() {

    CurlCommand curl = new CurlCommand().setUrl("/requestPath").addDataBinary("{'name':'John'}");

    assertThat(
        curl.asString(Platform.UNIX, true, false, true),
        equalTo("curl '/requestPath' --data-binary $'{\\'name\\':\\'John\\'}'"));

    assertThat(
        curl.asString(Platform.WINDOWS, true, false, true),
        equalTo("curl \"/requestPath\" --data-binary \"{'name':'John'}\""));
  }

  @Test
  public void shouldEscapeAtOnlyWhenFirstCharacterOnWindows() {

    CurlCommand curl = new CurlCommand().setUrl("/requestPath").addDataBinary("maciek@gmail.com");

    assertThat(
        curl.asString(Platform.UNIX, true, false, true),
        equalTo("curl '/requestPath' --data-binary 'maciek@gmail.com'"));

    assertThat(
        curl.asString(Platform.WINDOWS, true, false, true),
        equalTo("curl \"/requestPath\" --data-binary \"maciek@gmail.com\""));

    curl = new CurlCommand().setUrl("/requestPath").addDataBinary("@maciek.gmail.com");

    assertThat(
        curl.asString(Platform.UNIX, true, false, true),
        equalTo("curl '/requestPath' --data-binary $'\\x40maciek.gmail.com'"));

    assertThat(
        curl.asString(Platform.WINDOWS, true, false, true),
        equalTo("curl \"/requestPath\" --data-binary \"@maciek.gmail.com\""));
  }

  @Test
  public void shouldEscapeMultipleCharacters() {

    CurlCommand curl =
        new CurlCommand()
            .setUrl("http://testapi.com/post")
            .addDataBinary(
                ""
                    + "{\r\n"
                    + "   'name':'Administração',\r\n"
                    + "   'email':'admin@gmail.com',\r\n"
                    + "   'password':'abc%\"'\r\n"
                    + "}");

    assertThat(
        curl.asString(Platform.UNIX, true, false, true),
        equalTo(
            "curl 'http://testapi.com/post' --data-binary $'{\\r\\n   \\'name\\':"
                + "\\'Administra\\xe7\\xe3o\\',\\r\\n   \\'email\\':\\'admin@gmail.com\\',"
                + "\\r\\n   \\'password\\':\\'abc%\"\\'\\r\\n}'"));

    assertThat(
        curl.asString(Platform.WINDOWS, true, false, true),
        equalTo(
            "curl \"http://testapi.com/post\" --data-binary \"{\"^\r\n\r\n"
                + "\"   'name':'Administração',\"^\r\n\r\n"
                + "\"   'email':'admin@gmail.com',\"^\r\n\r\n"
                + "\"   'password':'abc\"%\"\"\"'\"^\r\n\r\n"
                + "\"}\""));
  }
}
