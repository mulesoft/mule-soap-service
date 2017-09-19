/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.interceptor;

import static java.lang.String.format;
import static org.apache.cxf.phase.Phase.PRE_PROTOCOL;
import static org.mule.service.soap.client.SoapCxfClient.MULE_HEADERS_KEY;
import static org.mule.service.soap.util.XmlTransformationUtils.nodeToString;

import org.mule.runtime.soap.api.exception.BadResponseException;
import org.mule.runtime.soap.api.message.SoapResponse;
import org.mule.service.soap.util.XmlTransformationException;

import com.google.common.collect.ImmutableMap;

import org.apache.cxf.binding.soap.SoapHeader;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.w3c.dom.Node;

/**
 * CXF out interceptor that collects the received SOAP headers in the response, transforms it and stores them in the response
 * message {@link Exchange} so then can be returned in a {@link SoapResponse}.
 *
 * @since 1.0
 */
public class OutputSoapHeadersInterceptor extends AbstractSoapInterceptor {

  public OutputSoapHeadersInterceptor() {
    super(PRE_PROTOCOL);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void handleMessage(SoapMessage message) throws Fault {
    ImmutableMap.Builder<String, String> headers = ImmutableMap.builder();
    message.getHeaders().stream()
        .filter(header -> header instanceof SoapHeader)
        .map(h -> (SoapHeader) h)
        .forEach(header -> headers.put(header.getName().getLocalPart(), getHeaderInputStream(header)));
    message.getExchange().put(MULE_HEADERS_KEY, headers.build());
  }

  private String getHeaderInputStream(SoapHeader h) {
    try {
      return nodeToString((Node) h.getObject());
    } catch (XmlTransformationException e) {
      throw new BadResponseException(format("Error while processing response header [%s]", h.getName()), e);
    }
  }
}
