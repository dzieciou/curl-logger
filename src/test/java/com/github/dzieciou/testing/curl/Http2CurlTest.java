package com.github.dzieciou.testing.curl;


import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Test(groups = "unit")
public class Http2CurlTest {

  @Test
  public void shouldPrintGetRequestProperly() throws Exception {
    HttpGet getRequest = new HttpGet("http://test.com:8080/items/query?x=y#z");
    assertThat(getNonWindowsHttp2Curl().generateCurl(getRequest),
        equalTo("curl 'http://test.com:8080/items/query?x=y#z' --compressed -k -v"));
  }

  @Test
  public void shouldPrintGetRequestForHttpsProperly() throws Exception {
    HttpGet getRequest = new HttpGet("https://test.com:8080/items/query?x=y#z");
    assertThat(getNonWindowsHttp2Curl().generateCurl(getRequest),
        equalTo("curl 'https://test.com:8080/items/query?x=y#z' --compressed -k -v"));
  }

  @Test
  public void shouldPrintBasicAuthnUserCredentials() throws Exception {
    HttpGet getRequest = new HttpGet("http://test.com:8080/items/query?x=y#z");
    String encodedCredentials = Base64.getEncoder().encodeToString("xx:yy".getBytes());
    getRequest.addHeader("Authorization", "Basic " + encodedCredentials);
    assertThat(getNonWindowsHttp2Curl().generateCurl(getRequest),
        equalTo("curl 'http://test.com:8080/items/query?x=y#z' -u 'xx:yy' --compressed -k -v"));
  }

  @Test
  public void shouldPrintBasicAuthnUserCredentialsWithoutPassword() throws Exception {
    HttpGet getRequest = new HttpGet("http://test.com:8080/items/query?x=y#z");
    String invalidEncodedCredentials = Base64.getEncoder().encodeToString("xx:".getBytes());
    getRequest.addHeader("Authorization", "Basic " + invalidEncodedCredentials);
    assertThat(getNonWindowsHttp2Curl().generateCurl(getRequest),
        equalTo("curl 'http://test.com:8080/items/query?x=y#z' -u 'xx:' --compressed -k -v"));
  }


  @Test
  public void shouldNotPrintInvalidBasicAuthnUserCredentials() throws Exception {
    HttpGet getRequest = new HttpGet("http://test.com:8080/items/query?x=y#z");
    String invalidEncodedCredentials = "xxx";
    getRequest.addHeader("Authorization", "Basic " + invalidEncodedCredentials);
    assertThat(getNonWindowsHttp2Curl().generateCurl(getRequest),
        equalTo("curl 'http://test.com:8080/items/query?x=y#z' -H 'Authorization: Basic xxx' --compressed -k -v"));
  }

  @Test
  public void shouldPrintPostRequestProperly() throws Exception {
    HttpPost postRequest = new HttpPost("http://google.pl/");
    List<NameValuePair> postParameters = new ArrayList<>();
    postParameters.add(new BasicNameValuePair("param1", "param1_value"));
    postParameters.add(new BasicNameValuePair("param2", "param2_value"));

    postRequest.setEntity(new UrlEncodedFormEntity(postParameters));
    postRequest.setHeader("Content-Type", "application/x-www-form-urlencoded");
    assertThat(getNonWindowsHttp2Curl().generateCurl(postRequest),
        equalTo(
            "curl 'http://google.pl/' -H 'Content-Type: application/x-www-form-urlencoded' --data-binary 'param1=param1_value&param2=param2_value' --compressed -k -v"));
  }

  @Test
  public void shouldPrintDeleteRequestProperly() throws Exception {
    HttpDelete deleteRequest = new HttpDelete("http://test.com/items/12345");
    assertThat(getNonWindowsHttp2Curl().generateCurl(deleteRequest),
        equalTo("curl 'http://test.com/items/12345' -X DELETE --compressed -k -v"));
  }

  @Test
  public void shouldPrintHeadRequestProperly() throws Exception {
    HttpHead headRequest = new HttpHead("http://test.com/items/12345");
    assertThat(getNonWindowsHttp2Curl().generateCurl(headRequest),
        equalTo("curl 'http://test.com/items/12345' -X HEAD --compressed -k -v"));
  }

  @Test
  public void shouldPrintMultipleCookiesInOneParameter() throws Exception {
    HttpHead headRequest = new HttpHead("http://test.com/items/12345");
    headRequest.setHeader("Cookie", "X=Y; A=B");
    assertThat(getNonWindowsHttp2Curl().generateCurl(headRequest),
        equalTo("curl 'http://test.com/items/12345' -X HEAD -b 'X=Y; A=B' --compressed -k -v"));
  }

  @Test
  public void shouldPrintMultipleCookieHeadersInMultipleParameters() throws Exception {
    HttpHead headRequest = new HttpHead("http://test.com/items/12345");
    headRequest.addHeader("Cookie", "X=Y; A=B");
    headRequest.addHeader("Cookie", "D=E");
    assertThat(getNonWindowsHttp2Curl().generateCurl(headRequest),
        equalTo("curl 'http://test.com/items/12345' -X HEAD -H 'Cookie: X=Y; A=B' -H 'Cookie: D=E' --compressed -k -v"));
  }


  @Test
  public void shouldPrintPutRequestProperly() throws Exception {
    HttpPut putRequest = new HttpPut("http://test.com/items/12345");
    putRequest.setEntity(new StringEntity("details={\"name\":\"myname\",\"age\":\"20\"}"));
    putRequest.setHeader("Content-Type", "application/json");
    assertThat(getNonWindowsHttp2Curl().generateCurl(putRequest),
        equalTo(
            "curl 'http://test.com/items/12345' -X PUT -H 'Content-Type: application/json' --data-binary 'details={\"name\":\"myname\",\"age\":\"20\"}' --compressed -k -v"));
  }

  @Test
  public void shouldPrintMultilineRequestProperly() throws Exception {

    // given
    HttpPost postRequest = new HttpPost("http://google.pl/");
    List<NameValuePair> postParameters = new ArrayList<>();
    postParameters.add(new BasicNameValuePair("param1", "param1_value"));
    postParameters.add(new BasicNameValuePair("param2", "param2_value"));
    postRequest.setEntity(new UrlEncodedFormEntity(postParameters));
    postRequest.setHeader("Content-Type", "application/x-www-form-urlencoded");

    // when
    Options options = Options.builder().targetPlatform(Platform.UNIX).useShortForm()
        .printMultiliner().build();

    // then
    assertThat(new Http2Curl(options).generateCurl(postRequest),
        equalTo(
            "curl 'http://google.pl/' \\\n  -H 'Content-Type: application/x-www-form-urlencoded' \\\n  --data-binary 'param1=param1_value&param2=param2_value' \\\n  --compressed \\\n  -k \\\n  -v"));
  }

  @Test
  public void shouldWriteParametersInLongForm() throws Exception {

    // given
    HttpGet getRequest = new HttpGet("http://test.com:8080/items/query?x=y#z");
    getRequest.addHeader("Host", "H");

    // when
    Options options = Options.builder().targetPlatform(Platform.UNIX).useLongForm()
        .printSingleliner().build();

    // then
    assertThat(new Http2Curl(options).generateCurl(getRequest),
        equalTo(
            "curl 'http://test.com:8080/items/query?x=y#z' --header 'Host: H' --compressed --insecure --verbose"));
  }

  public Http2Curl getNonWindowsHttp2Curl() {
    return new Http2Curl(
        Options.builder().targetPlatform(Platform.UNIX).useShortForm().printSingleliner().build());
  }

}
