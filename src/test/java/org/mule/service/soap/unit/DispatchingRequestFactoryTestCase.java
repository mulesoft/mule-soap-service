/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.unit;

import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.junit.Test;
import org.mule.runtime.extension.api.soap.message.DispatchingRequest;
import org.mule.service.soap.interceptor.DispatchingRequestFactory;
import org.mule.service.soap.interceptor.MessageDispatcherInterceptor;
import sun.nio.ch.IOUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import static java.util.Collections.singletonMap;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.service.soap.client.SoapCxfClient.MULE_TRANSPORT_HEADERS_KEY;
import static org.mule.service.soap.interceptor.SoapActionInterceptor.SOAP_ACTION;


public class DispatchingRequestFactoryTestCase {

  private static final String CONTENT = "<a>content</a>";
  private static final String SOAP_ACTION_VALUE = "noOPE";

  @Test
  public void checkOverrideDefaultHeaders() throws IOException {
    Message messageMock = getMessageMock();
    DispatchingRequest dispatchingRequest = DispatchingRequestFactory.createDispatchingRequest(messageMock);
    assertThat(dispatchingRequest.getHeader(SOAP_ACTION).get(), is(SOAP_ACTION_VALUE));
  }

  private Message getMessageMock() throws IOException {
    Exchange exchangeMock = mock(Exchange.class);
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    os.write(CONTENT.getBytes());
    when(exchangeMock.get(MULE_TRANSPORT_HEADERS_KEY)).thenReturn(singletonMap(SOAP_ACTION.toLowerCase(), SOAP_ACTION_VALUE));
    Message messageMock = mock(Message.class);
    when(messageMock.getContent(OutputStream.class)).thenReturn(os);
    when(messageMock.getExchange()).thenReturn(exchangeMock);
    return messageMock;
  }
}
