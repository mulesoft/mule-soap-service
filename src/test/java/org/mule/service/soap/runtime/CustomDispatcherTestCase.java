/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.runtime;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.soap.api.message.SoapRequest.builder;
import static org.mule.service.soap.SoapTestUtils.assertSimilarXml;
import static org.mule.service.soap.client.TestSoapClient.getDefaultConfiguration;

import org.mule.runtime.extension.api.soap.message.DispatchingRequest;
import org.mule.runtime.extension.api.soap.message.DispatchingResponse;
import org.mule.runtime.extension.api.soap.message.MessageDispatcher;
import org.mule.runtime.soap.api.message.SoapRequest;
import org.mule.runtime.soap.api.message.SoapResponse;
import org.mule.service.soap.AbstractSoapServiceTestCase;
import org.mule.service.soap.client.TestSoapClient;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.util.Map;

public class CustomDispatcherTestCase extends AbstractSoapServiceTestCase {

  private static final String RESPONSE = "<text>RESPONSE</text>";

  @Test
  public void customDispatcher() throws Exception {
    SoapResponse response = getTestClient().consume(SoapRequest.empty("noParams"));
    assertSimilarXml(RESPONSE, response.getContent());
  }

  @Test
  public void transportHeaders() {
    Map<String, String> headers = ImmutableMap.<String, String>builder().put("H1", "H1Value").put("H2", "H2Value").build();
    SoapResponse response = getTestClient().consume(builder().withOperation("noParams").withTransportHeaders(headers).build());
    response.getTransportHeaders().forEach((k, v) -> {
      assertThat(headers.containsKey(k), is(true));
      assertThat(headers.containsValue(v), is(true));
    });
  }

  private TestSoapClient getTestClient() {
    return new TestSoapClient(getDefaultConfiguration(server.getDefaultAddress())
        .withVersion(soapVersion)
        .withDispatcher(new TestDispatcher())
        .build());
  }

  public class TestDispatcher implements MessageDispatcher {

    @Override
    public DispatchingResponse dispatch(DispatchingRequest request) {
      String envelope = "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">"
          + "<s:Body>" + RESPONSE + "</s:Body>"
          + "</s:Envelope>";
      return new DispatchingResponse(new ByteArrayInputStream(envelope.getBytes()), request.getHeaders());
    }
  }
}
