/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.interceptor;

import javax.xml.soap.SOAPConstants;
import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.StaxInInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

/**
 * CXF interceptor that wraps the XML Stream with a decorator that is able to restore namespace declarations for fragments in the
 * response message.
 *
 * @since 1.0
 */
public class NamespaceSaverStaxInterceptor extends AbstractPhaseInterceptor<Message> {

  public NamespaceSaverStaxInterceptor() {
    super(Phase.POST_STREAM);
    getAfter().add(StreamClosingInterceptor.class.getName());
    getAfter().add(StaxInInterceptor.class.getName());
  }

  public void handleMessage(Message message) throws Fault {
    XMLStreamReader reader = message.getContent(XMLStreamReader.class);

    if (reader != null) {
      NamespaceRestorerXMLStreamReader replacement = new NamespaceRestorerXMLStreamReader(reader)
          .blockList(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE)
          .blockList(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE);

      message.setContent(XMLStreamReader.class, replacement);
      message.setContent(NamespaceRestorerXMLStreamReader.class, replacement);
    }
  }
}
