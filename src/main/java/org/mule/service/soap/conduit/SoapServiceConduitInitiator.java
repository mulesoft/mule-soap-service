/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.conduit;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.apache.cxf.ws.addressing.EndpointReferenceUtils.setServiceAndPortName;

import org.apache.cxf.Bus;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A {@link ConduitInitiator} implementation that creates {@link SoapServiceConduit} instances.
 *
 * @since 1.0
 */
public class SoapServiceConduitInitiator implements ConduitInitiator {

  public static final String[] SOAP_SERVICE_KNOWN_PROTOCOLS = new String[] {"http://", "https://", "jms://"};

  @Override
  public Conduit getConduit(EndpointInfo targetInfo, Bus bus) throws IOException {
    return getConduit(targetInfo, null, bus);
  }

  @Override
  public Conduit getConduit(EndpointInfo localInfo, EndpointReferenceType target, Bus bus) throws IOException {
    return new SoapServiceConduit(getTargetReference(localInfo, target));
  }

  @Override
  public Set<String> getUriPrefixes() {
    return new HashSet<>(asList(SOAP_SERVICE_KNOWN_PROTOCOLS));
  }

  @Override
  public List<String> getTransportIds() {
    return Stream.of("http://schemas.xmlsoap.org/soap/http").collect(toList());
  }

  private EndpointReferenceType getTargetReference(EndpointInfo endpointInfo, EndpointReferenceType referenceType) {
    if (referenceType != null) {
      return referenceType;
    }
    EndpointReferenceType ref = new EndpointReferenceType();
    AttributedURIType address = new AttributedURIType();
    address.setValue(endpointInfo.getAddress());
    ref.setAddress(address);
    if (endpointInfo.getService() != null) {
      setServiceAndPortName(ref, endpointInfo.getService().getName(), endpointInfo.getName().getLocalPart());
    }
    return ref;
  }
}
