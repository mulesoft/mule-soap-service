/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.client;

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.soap.api.client.SoapClient;
import org.mule.runtime.soap.api.client.SoapClientConfiguration;
import org.mule.runtime.soap.api.client.SoapClientConfigurationBuilder;
import org.mule.runtime.soap.api.client.metadata.SoapMetadataResolver;
import org.mule.runtime.soap.api.message.SoapRequest;
import org.mule.runtime.soap.api.message.SoapResponse;
import org.mule.service.soap.SoapServiceImplementation;
import org.junit.rules.ExternalResource;

public class TestSoapClient extends ExternalResource implements SoapClient {

  private static final String DEFAULT_TEST_SERVICE_NAME = "TestService";
  private static final String DEFAULT_TEST_PORT_NAME = "TestPort";

  private final SoapClient soapClient;

  public TestSoapClient(SoapClientConfiguration config) {
    this.soapClient = getClient(config);
  }

  private SoapClient getClient(SoapClientConfiguration config) {
    try {
      return new SoapServiceImplementation().getClientFactory().create(config);
    } catch (ConnectionException e) {
      throw new RuntimeException(e);
    }
  }

  public static SoapClientConfigurationBuilder getDefaultConfiguration(String address) {
    return SoapClientConfiguration.builder()
        .withAddress(address)
        .withWsdlLocation(address + "?wsdl")
        .withService(DEFAULT_TEST_SERVICE_NAME)
        .withPort(DEFAULT_TEST_PORT_NAME);
  }

  @Override
  public SoapResponse consume(SoapRequest request) {
    return soapClient.consume(request);
  }

  @Override
  public SoapMetadataResolver getMetadataResolver() {
    return soapClient.getMetadataResolver();
  }

  @Override
  public void start() throws MuleException {}

  @Override
  public void stop() throws MuleException {}
}
