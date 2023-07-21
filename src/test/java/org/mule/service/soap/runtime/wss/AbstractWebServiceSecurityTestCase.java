/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.runtime.wss;

import static org.mule.runtime.soap.api.message.SoapRequest.builder;

import org.mule.runtime.soap.api.message.SoapRequest;
import org.mule.service.soap.AbstractSoapServiceTestCase;

import org.junit.Test;
import io.qameta.allure.Description;

public abstract class AbstractWebServiceSecurityTestCase extends AbstractSoapServiceTestCase {

  @Test
  @Description("Consumes a simple operation of a secured web service and expects a valid response")
  public void expectedSecuredRequest() throws Exception {
    SoapRequest req = builder()
        .content("<con:echo xmlns:con=\"http://service.soap.service.mule.org/\"><text>test</text></con:echo>")
        .operation("echo")
        .build();
    client.consume(req);
  }
}
