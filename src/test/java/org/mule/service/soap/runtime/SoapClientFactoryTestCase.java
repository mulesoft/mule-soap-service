/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.runtime;

import static org.mockito.Mockito.mock;
import static org.mule.runtime.soap.api.client.SoapClientConfiguration.builder;
import static org.mule.test.allure.AllureConstants.WscFeature.WSC_EXTENSION;
import static java.lang.Thread.currentThread;

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.soap.message.MessageDispatcher;
import org.mule.runtime.soap.api.client.SoapClientFactory;
import org.mule.service.soap.AbstractSoapServiceTestCase;
import org.mule.service.soap.SoapServiceImplementation;

import java.net.URL;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(WSC_EXTENSION)
@Story("Connection")
public class SoapClientFactoryTestCase extends AbstractSoapServiceTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private SoapClientFactory factory = new SoapServiceImplementation().getClientFactory();

  @Test
  @Description("Tries to instantiate a connection with an RPC WSDL and fails.")
  public void rpcWsdlFails() throws Exception {
    expectedException.expect(ConnectionException.class);
    expectedException.expectMessage("RPC WSDLs are not supported");
    URL resource = currentThread().getContextClassLoader().getResource("wsdl/rpc.wsdl");
    factory.create(builder()
        .withPort("SoapResponderPortType")
        .withService("SoapResponder")
        .withDispatcher(mock(MessageDispatcher.class))
        .withWsdlLocation(resource.getPath())
        .build());
  }

  @Test
  @Description("Tries to create a client with an invalid service")
  public void invalidService() throws Exception {
    expectedException.expect(ConnectionException.class);
    expectedException.expectMessage("Service [INVALID_SERVICE] is not defined in the wsdl");
    URL resource = currentThread().getContextClassLoader().getResource("wsdl/simple-service.wsdl");
    factory.create(builder()
        .withPort("INVALID PORT")
        .withService("INVALID_SERVICE")
        .withDispatcher(mock(MessageDispatcher.class))
        .withWsdlLocation(resource.getPath())
        .build());
  }

  @Test
  @Description("Tries to create a client with an invalid service")
  public void invalidPort() throws Exception {
    expectedException.expect(ConnectionException.class);
    expectedException.expectMessage("Port [INVALID PORT] not found in service [TestService]");
    URL resource = currentThread().getContextClassLoader().getResource("wsdl/simple-service.wsdl");
    factory.create(builder()
        .withPort("INVALID PORT")
        .withService("TestService")
        .withDispatcher(mock(MessageDispatcher.class))
        .withWsdlLocation(resource.getPath())
        .build());
  }
}
