/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.client;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.mule.service.soap.conduit.SoapServiceConduitInitiator.SOAP_SERVICE_KNOWN_PROTOCOLS;

import org.mule.metadata.xml.api.XmlTypeLoader;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.soap.api.client.SoapClient;
import org.mule.runtime.soap.api.client.SoapClientConfiguration;
import org.mule.runtime.soap.api.client.SoapClientFactory;
import org.mule.service.soap.introspection.ServiceDefinition;
import org.apache.cxf.endpoint.Client;

/**
 * {@link SoapClientFactory} implementation that creates {@link SoapCxfClient} instances.
 *
 * @since 1.0
 */
public class SoapCxfClientFactory implements SoapClientFactory {

  private CxfClientProvider cxfClientProvider = new CxfClientProvider();

  /**
   * Creates a new instance of a {@link SoapCxfClient} for the given address ans soap version.
   *
   * @throws ConnectionException if the client couldn't be created.
   */
  @Override
  public SoapClient create(SoapClientConfiguration config) throws ConnectionException {
    ServiceDefinition definition = getWsdlDefinition(config);
    XmlTypeLoader xmlTypeLoader = new XmlTypeLoader(definition.getSchemas());
    Client client = cxfClientProvider.getClient(config);
    return new SoapCxfClient(client, definition, xmlTypeLoader, getAddress(config, definition),
                             config.getDispatcher(), config.getVersion(), config.getEncoding(), config.isMtomEnabled());
  }

  private String getAddress(SoapClientConfiguration config, ServiceDefinition definition) throws ConnectionException {
    String address = config.getAddress() != null ? config.getAddress() : findAddress(definition);
    String protocolSeparator = "://";
    if (address.contains(protocolSeparator)) {
      String protocol = address.substring(0, address.indexOf(protocolSeparator));
      if (stream(SOAP_SERVICE_KNOWN_PROTOCOLS).noneMatch(p -> p.startsWith(protocol))) {
        throw new IllegalArgumentException(format("cannot create a dispatcher for address [%s], known protocols are [%s]",
                                                  address, stream(SOAP_SERVICE_KNOWN_PROTOCOLS).collect(joining(", "))));
      }
    }
    return address;
  }

  private ServiceDefinition getWsdlDefinition(SoapClientConfiguration config) throws ConnectionException {
    String wsdlLocation = config.getWsdlLocation();
    ServiceDefinition definition =
        new ServiceDefinition(wsdlLocation, config.getService(), config.getPort(), config.getLocator());
    if (definition.isRpcStyle()) {
      // TODO: MULE-11082  Support RPC Style - CXF DOES NOT SUPPORT RPC, if supported a new RPC Client should be created.
      throw new ConnectionException(format("The provided WSDL [%s] is RPC style, RPC WSDLs are not supported", wsdlLocation));
    }
    return definition;
  }

  private String findAddress(ServiceDefinition wsdldefinition) throws ConnectionException {
    return wsdldefinition.getSoapAddress()
        .orElseThrow(() -> new ConnectionException("No address was specified and no one was found for the given configuration"));
  }
}
