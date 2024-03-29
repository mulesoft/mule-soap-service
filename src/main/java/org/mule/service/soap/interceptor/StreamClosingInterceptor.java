/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.interceptor;

import static org.apache.cxf.phase.Phase.POST_STREAM;

import org.mule.service.soap.xml.stax.DelegateXMLStreamReader;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.StaxInInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;

/**
 * CXF interceptor that replaces the original XMLStreamReader with another one which closes the underlying {@link InputStream}
 * that carried the request.
 *
 * @since 1.0
 */
public class StreamClosingInterceptor extends AbstractPhaseInterceptor<Message> {

  public StreamClosingInterceptor() {
    super(POST_STREAM);
    addAfter(StaxInInterceptor.class.getName());
  }

  public void handleMessage(final Message message) throws Fault {
    XMLStreamReader xsr = message.getContent(XMLStreamReader.class);
    final InputStream is = message.getContent(InputStream.class);
    DelegateXMLStreamReader xsr2 = new DelegateXMLStreamReader(xsr) {

      @Override
      public void close() throws XMLStreamException {
        super.close();
        try {
          is.close();
        } catch (IOException e) {
          throw new XMLStreamException(e);
        }
      }
    };
    message.setContent(XMLStreamReader.class, xsr2);
  }
}

