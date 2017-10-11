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
import static org.mule.wsdl.parser.model.WsdlStyle.RPC;

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.soap.api.client.SoapClient;
import org.mule.runtime.soap.api.client.SoapClientConfiguration;
import org.mule.runtime.soap.api.client.SoapClientFactory;
import org.mule.runtime.soap.api.transport.TransportResourceLocator;
import org.mule.wsdl.parser.WsdlParser;
import org.mule.wsdl.parser.locator.ResourceLocator;
import org.mule.wsdl.parser.model.PortModel;
import org.mule.wsdl.parser.model.WsdlModel;

import java.io.InputStream;
import java.net.URL;

import org.apache.cxf.endpoint.Client;
import org.jetbrains.annotations.NotNull;

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
    WsdlModel wsdlDefinition = getWsdlDefinition(config);
    Client client = cxfClientProvider.getClient(config);
    PortModel port = wsdlDefinition.getService(config.getService()).getPort(config.getPort());
    return new SoapCxfClient(client,
                             wsdlDefinition,
                             port,
                             getAddress(config, port.getAddress()),
                             config.getDispatcher(),
                             config.getVersion(),
                             config.getEncoding(),
                             config.isMtomEnabled());
  }

  private String getAddress(SoapClientConfiguration config, URL serviceAddress) throws ConnectionException {

    if (config.getAddress() == null && serviceAddress == null) {
      throw new ConnectionException("No address was specified and no one was found for the given configuration");
    }

    String address = config.getAddress() != null ? config.getAddress() : serviceAddress.toString();
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

  private WsdlModel getWsdlDefinition(SoapClientConfiguration config) throws ConnectionException {
    String location = config.getWsdlLocation();
    WsdlModel wsdlModel = WsdlParser.Companion.parse(location, new ResourceLocatorAdapter(config.getLocator()));
    if (RPC.equals(wsdlModel.getStyle())) {
      throw new ConnectionException(format("The provided WSDL [%s] is RPC style, RPC WSDLs are not supported",
                                           location));
    }
    return wsdlModel;
  }

  private class ResourceLocatorAdapter implements ResourceLocator {

    private final TransportResourceLocator locator;

    public ResourceLocatorAdapter(TransportResourceLocator locator) {
      this.locator = locator;
    }

    @Override
    public boolean handles(String s) {
      return locator.handles(s);
    }

    @NotNull
    @Override
    public InputStream getResource(String s) {
      return locator.getResource(s);
    }
  }
}
