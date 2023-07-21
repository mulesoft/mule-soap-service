/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.runtime;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.soap.api.message.SoapRequest.builder;
import static org.mule.service.soap.SoapTestUtils.assertSimilarXml;
import static org.mule.service.soap.client.TestSoapClient.getDefaultConfiguration;

import org.mule.runtime.extension.api.soap.message.DispatchingRequest;
import org.mule.runtime.extension.api.soap.message.DispatchingResponse;
import org.mule.runtime.extension.api.soap.message.MessageDispatcher;
import org.mule.runtime.soap.api.message.ImmutableSoapRequest;
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
    ImmutableSoapRequest request = builder().operation("noParams").transportHeaders(ImmutableMap.<String, String>builder()
        .put("H1", "H1Value")
        .put("H2", "H2Value")
        .build()).build();
    SoapResponse response = getTestClient().consume(request);
    Map<String, String> transportHeaders = response.getTransportHeaders();
    assertThat(transportHeaders.entrySet(), hasSize(4));
    assertThat(transportHeaders, hasEntry("H1", "H1Value"));
    assertThat(transportHeaders, hasEntry("H2", "H2Value"));
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
