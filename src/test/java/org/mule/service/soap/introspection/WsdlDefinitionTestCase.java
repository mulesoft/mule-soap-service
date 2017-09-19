/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.introspection;

import static java.lang.Thread.currentThread;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.mule.runtime.soap.api.exception.InvalidWsdlException;
import org.mule.service.soap.server.BasicAuthHttpServer;
import org.mule.service.soap.service.Soap11Service;
import org.mule.tck.junit4.rule.DynamicPort;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class WsdlDefinitionTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Rule
  public DynamicPort port = new DynamicPort("testPort");

  @Test
  public void getWsdlStyleFromOperations() throws URISyntaxException {
    String resourceLocation = getResourceLocation("wsdl/document.wsdl");
    ServiceDefinition definition = new ServiceDefinition(resourceLocation, "Dilbert", "DilbertSoap");
    assertThat(definition.isDocumentStyle(), is(true));
  }

  @Test
  public void getWsdlStyleDefault() throws URISyntaxException {
    String resourceLocation = getResourceLocation("wsdl/no-style-defined.wsdl");
    ServiceDefinition definition = new ServiceDefinition(resourceLocation, "messagingService", "messagingPort");
    assertThat(definition.isDocumentStyle(), is(true));
  }

  @Test
  public void getWsdlStyleFromBinding() throws URISyntaxException {
    String resourceLocation = getResourceLocation("wsdl/rpc.wsdl");
    ServiceDefinition definition = new ServiceDefinition(resourceLocation, "SoapResponder", "SoapResponderPortType");
    assertThat(definition.isRpcStyle(), is(true));
  }

  @Test
  public void cannotAccess() throws Exception {
    expectedException.expect(InvalidWsdlException.class);
    expectedException.expectMessage("faultCode=OTHER_ERROR: Unable to locate document at");
    BasicAuthHttpServer server = new BasicAuthHttpServer(port.getNumber(), null, null, new Soap11Service());
    String resourceLocation = server.getDefaultAddress() + "?wsdl";
    new ServiceDefinition(resourceLocation, "TestService", "TestPort");
    server.stop();
  }

  @Test
  public void protectedWsdl() throws Exception {
    BasicAuthHttpServer server = new BasicAuthHttpServer(port.getNumber(), null, null, new Soap11Service());
    HttpBasicAuthResourceLocator resourceLocator = new HttpBasicAuthResourceLocator();
    resourceLocator.start();
    String resourceLocation = server.getDefaultAddress() + "?wsdl";
    ServiceDefinition definition = new ServiceDefinition(resourceLocation, "TestService", "TestPort", resourceLocator);
    resourceLocator.stop();
    server.stop();
    assertThat(definition.isDocumentStyle(), is(true));
  }

  private String getResourceLocation(String name) throws URISyntaxException {
    return new File(currentThread().getContextClassLoader().getResource(name).toURI()).getPath();
  }
}
