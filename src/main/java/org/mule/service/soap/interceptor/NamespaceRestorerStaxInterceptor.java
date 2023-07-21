/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.interceptor;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.StaxInInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

/**
 * CXF interceptor that makes use of a {@link NamespaceRestorerXMLStreamReader} to allow the first element of the body to carry
 * all the namespaces from the parent nodes.
 * <p>
 * This interceptor works with {@link NamespaceSaverStaxInterceptor}
 *
 * @since 1.0
 */
public class NamespaceRestorerStaxInterceptor extends AbstractPhaseInterceptor<Message> {

  public NamespaceRestorerStaxInterceptor() {
    super(Phase.PRE_INVOKE);
    getAfter().add(StaxInInterceptor.class.getName());
  }

  public void handleMessage(Message message) throws Fault {
    NamespaceRestorerXMLStreamReader reader = message.getContent(NamespaceRestorerXMLStreamReader.class);
    reader.restoreNamespaces();
  }
}


