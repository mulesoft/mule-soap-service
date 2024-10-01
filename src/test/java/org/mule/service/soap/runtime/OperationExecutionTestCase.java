/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.runtime;

import static java.lang.Thread.currentThread;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mule.runtime.api.metadata.MediaType.APPLICATION_XML;
import static org.mule.runtime.soap.api.message.SoapRequest.builder;
import static org.mule.service.soap.SoapTestUtils.assertSimilarXml;
import static org.mule.service.soap.SoapTestXmlValues.ECHO;
import static org.mule.service.soap.SoapTestXmlValues.ECHO_ACCOUNT;
import static org.mule.service.soap.SoapTestXmlValues.ECHO_HEADERS;
import static org.mule.service.soap.SoapTestXmlValues.HEADER_IN;
import static org.mule.service.soap.SoapTestXmlValues.HEADER_INOUT;
import static org.mule.service.soap.SoapTestXmlValues.HEADER_OUT;
import static org.mule.service.soap.SoapTestXmlValues.NO_PARAMS;
import static org.mule.service.soap.SoapTestXmlValues.ONE_WAY;
import static org.mule.service.soap.client.TestSoapClient.getDefaultConfiguration;
import static org.mule.test.allure.AllureConstants.WscFeature.WSC_EXTENSION;

import org.mule.runtime.soap.api.client.SoapClient;
import org.mule.runtime.soap.api.message.ImmutableSoapRequest;
import org.mule.runtime.soap.api.message.SoapRequest;
import org.mule.runtime.soap.api.message.SoapResponse;
import org.mule.service.soap.AbstractSoapServiceTestCase;
import org.mule.service.soap.client.TestSoapClient;

import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

@Feature(WSC_EXTENSION)
@Story("Operation Execution")
public class OperationExecutionTestCase extends AbstractSoapServiceTestCase {

  @Test
  @Description("Consumes an operation that expects a simple type and returns a simple type")
  public void simpleOperation() throws Exception {
    testSimpleOperation(client);
  }

  @Test
  @Description("Consumes an operation using a connection that uses a local .wsdl file")
  public void echoWithLocalWsdl() throws Exception {
    URL wsdl = currentThread().getContextClassLoader().getResource("wsdl/simple-service.wsdl");
    TestSoapClient localWsdlClient = new TestSoapClient(getDefaultConfiguration(server.getDefaultAddress())
        .withWsdlLocation(wsdl.getPath())
        .withDispatcher(dispatcher)
        .withVersion(soapVersion).build());
    testSimpleOperation(localWsdlClient);
  }

  @Test
  @Description("Consumes an operation that expects an input and a set of headers and returns a simple type and a set of headers")
  public void simpleOperationWithHeaders() throws Exception {
    Map<String, String> headers = ImmutableMap.<String, String>builder()
        .put(HEADER_IN, testValues.getHeaderIn())
        .put(HEADER_INOUT, testValues.getHeaderInOutRequest())
        .build();

    ImmutableSoapRequest req =
        builder().content(testValues.getEchoWithHeadersRequest())
            .operation(ECHO_HEADERS)
            .soapHeaders(headers)
            .contentType(APPLICATION_XML)
            .build();

    SoapResponse response = client.consume(req);
    assertSimilarXml(response.getSoapHeaders().get(HEADER_OUT), testValues.getHeaderOut());
    assertSimilarXml(response.getSoapHeaders().get(HEADER_INOUT), testValues.getHeaderInOutResponse());
    assertSimilarXml(testValues.getEchoWithHeadersResponse(), response.getContent());
  }

  @Test
  @Description("Consumes an operation that expects 2 parameters (a simple one and a complex one) and returns a complex type")
  public void complexTypeOperation() throws Exception {
    ImmutableSoapRequest req = builder().content(testValues.getEchoAccountRequest()).operation(ECHO_ACCOUNT).build();
    SoapResponse response = client.consume(req);
    assertThat(response.getSoapHeaders().isEmpty(), is(true));
    assertSimilarXml(testValues.getEchoAccountResponse(), response.getContent());
  }

  @Test
  @Description("Consumes an operation that expects no parameters and returns a simple type")
  public void noParamsOperation() throws Exception {
    SoapRequest req = builder().content(testValues.getNoParamsRequest()).operation(NO_PARAMS).build();
    testNoParams(req);
  }

  @Test
  @Description("Consumes an operation that expects no parameters and returns a simple type")
  public void large() throws Exception {
    SoapRequest req = builder().operation("large").build();
    InputStream response = client.consume(req).getContent();
    String largeContent = IOUtils.toString(currentThread().getContextClassLoader().getResource("large.json").openStream());
    assertSimilarXml(testValues.buildXml("largeResponse", "<largeResponse>" + largeContent + "</largeResponse>"), response);
  }

  @Test
  @Description("Consumes an operation that expects no parameters auto-generating the request and returns a simple type")
  public void noParamsOperationWithoutXmlPayload() throws Exception {
    testNoParams(SoapRequest.empty(NO_PARAMS));
  }

  @Test
  @Description("Consumes an operation and checks the output transport headers")
  public void transportHeaders() {
    SoapRequest request = builder().content(testValues.getEchoResquest()).operation(ECHO).build();
    SoapResponse response = client.consume(request);
    Map<String, String> transportHeaders = response.getTransportHeaders();
    assertThat(transportHeaders.entrySet(), hasSize(4));
  }

  @Test
  @Description("Consumes an operation that is one way, without response")
  public void oneWayOperation() throws IOException {
    SoapRequest req = builder().operation(ONE_WAY).content(testValues.getOneWayRequest()).build();
    SoapResponse response = client.consume(req);
    assertThat(IOUtils.toString(response.getContent()), is(""));
    assertThat(response.getContentType(), is(nullValue()));
  }

  private void testSimpleOperation(SoapClient client) throws Exception {
    SoapRequest request = builder().content(testValues.getEchoResquest()).operation(ECHO).build();
    SoapResponse response = client.consume(request);
    assertThat(response.getSoapHeaders().isEmpty(), is(true));
    assertSimilarXml(testValues.getEchoResponse(), response.getContent());
  }

  private void testNoParams(SoapRequest request) throws Exception {
    SoapResponse response = client.consume(request);
    assertThat(response.getSoapHeaders().isEmpty(), is(true));
    assertSimilarXml(testValues.getNoParamsResponse(), response.getContent());
  }
}
