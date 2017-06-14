/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap;

import static java.util.Collections.emptyList;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.extension.api.soap.message.MessageDispatcher;
import org.mule.runtime.extension.api.soap.security.SecurityStrategy;
import org.mule.runtime.soap.api.SoapVersion;
import org.mule.runtime.soap.api.client.SoapClient;
import org.mule.runtime.soap.api.client.SoapClientConfiguration;
import org.mule.runtime.soap.api.client.SoapClientConfigurationBuilder;
import org.mule.runtime.soap.api.client.metadata.SoapMetadataResolver;
import org.mule.runtime.soap.api.message.SoapRequest;
import org.mule.runtime.soap.api.message.SoapResponse;
import org.mule.runtime.soap.api.message.dispatcher.DefaultHttpMessageDispatcher;
import org.mule.service.http.impl.service.HttpServiceImplementation;
import org.mule.tck.SimpleUnitTestSupportSchedulerService;

import java.util.List;

import org.junit.rules.ExternalResource;

public class TestSoapClient extends ExternalResource implements SoapClient {

  private final SoapClient soapClient;
  private final MessageDispatcher dispatcher;

  public TestSoapClient(String wsdlLocation,
                        String address,
                        String service,
                        String port,
                        boolean mtom,
                        List<SecurityStrategy> strategies,
                        SoapVersion version,
                        MessageDispatcher dispatcher) {
    HttpServiceImplementation httpService = new HttpServiceImplementation(new SimpleUnitTestSupportSchedulerService());
    SoapServiceImplementation soapService = new SoapServiceImplementation();
    try {
      this.dispatcher = dispatcher != null ? dispatcher : new DefaultHttpMessageDispatcher(httpService);
      try {
        this.dispatcher.initialise();
      } catch (InitialisationException e) {
        throw new RuntimeException("Cannot initialize dispatcher");
      }
      SoapClientConfigurationBuilder config = SoapClientConfiguration.builder()
          .withWsdlLocation(wsdlLocation)
          .withAddress(address)
          .withDispatcher(this.dispatcher)
          .withService(service)
          .withPort(port)
          .withVersion(version);

      if (mtom) {
        config.enableMtom();
      }

      strategies.forEach(config::withSecurity);

      this.soapClient = soapService.getClientFactory().create(config.build());
    } catch (ConnectionException e) {
      throw new RuntimeException(e);
    }
  }

  public TestSoapClient(String wsdlLocation, String address, SoapVersion version) {
    this(wsdlLocation, address, "TestService", "TestPort", false, emptyList(), version, null);
  }

  public TestSoapClient(String wsdlLocation, String address, SoapVersion version, MessageDispatcher dispatcher) {
    this(wsdlLocation, address, "TestService", "TestPort", false, emptyList(), version, dispatcher);
  }

  TestSoapClient(String location,
                 String address,
                 boolean mtom,
                 List<SecurityStrategy> securityStrategies,
                 SoapVersion version) {
    this(location, address, "TestService", "TestPort", mtom, securityStrategies, version, null);
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
  public void start() {

  }

  @Override
  public void stop() {
    dispatcher.dispose();
  }
}