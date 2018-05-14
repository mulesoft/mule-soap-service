/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.interceptor;

import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.mule.runtime.extension.api.soap.message.DispatchingRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static org.apache.cxf.message.Message.CONTENT_TYPE;
import static org.mule.service.soap.client.SoapCxfClient.MULE_SOAP_ACTION;
import static org.mule.service.soap.client.SoapCxfClient.MULE_TRANSPORT_HEADERS_KEY;
import static org.mule.service.soap.client.SoapCxfClient.MULE_WSC_ADDRESS;
import static org.mule.service.soap.interceptor.SoapActionInterceptor.SOAP_ACTION;

/**
 * Creates {@link DispatchingRequest} instances given a {@link Message}.
 *
 * @since 1.1
 */
public class DispatchingRequestFactory {

  public static DispatchingRequest createDispatchingRequest(Message message) {
    Exchange exchange = message.getExchange();
    String action = (String) exchange.get(MULE_SOAP_ACTION);
    Map<String, String> headers = new TreeMap<>(CASE_INSENSITIVE_ORDER);
    headers.put(SOAP_ACTION, action);
    // It's important that content type is bundled with the headers
    headers.put(CONTENT_TYPE, (String) message.get(CONTENT_TYPE));
    headers.putAll((Map) exchange.get(MULE_TRANSPORT_HEADERS_KEY));
    InputStream content = new ByteArrayInputStream(message.getContent(OutputStream.class).toString().getBytes());
    return new DispatchingRequest(content, (String) exchange.get(MULE_WSC_ADDRESS), headers);
  }
}
