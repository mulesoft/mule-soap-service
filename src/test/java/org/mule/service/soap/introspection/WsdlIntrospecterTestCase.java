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

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Test;

public class WsdlIntrospecterTestCase {

  @Test
  public void getWsdlStyleFromOperations() throws URISyntaxException {
    String resourceLocation = getResourceLocation("wsdl/document.wsdl");
    WsdlDefinition introspecter = new WsdlDefinition(resourceLocation, "Dilbert", "DilbertSoap");
    assertThat(introspecter.isDocumentStyle(), is(true));
  }

  @Test
  public void getWsdlStyleDefault() throws URISyntaxException {
    String resourceLocation = getResourceLocation("wsdl/no-style-defined.wsdl");
    WsdlDefinition introspecter = new WsdlDefinition(resourceLocation, "messagingService", "messagingPort");
    assertThat(introspecter.isDocumentStyle(), is(true));
  }

  @Test
  public void getWsdlStyleFromBinding() throws URISyntaxException {
    String resourceLocation = getResourceLocation("wsdl/rpc.wsdl");
    WsdlDefinition introspecter = new WsdlDefinition(resourceLocation, "SoapResponder", "SoapResponderPortType");
    assertThat(introspecter.isRpcStyle(), is(true));
  }

  private String getResourceLocation(String name) throws URISyntaxException {
    return new File(currentThread().getContextClassLoader().getResource(name).toURI()).getPath();
  }
}
