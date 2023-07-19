/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.interceptor;

import static java.lang.Boolean.TRUE;
import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.util.Collections.emptyList;
import static org.apache.cxf.interceptor.StaxInEndingInterceptor.STAX_IN_NOCLOSE;
import static org.apache.cxf.message.Message.CONTENT_TYPE;
import static org.apache.cxf.message.Message.ENCODING;
import static org.apache.cxf.phase.Phase.SEND_ENDING;
import static org.mule.service.soap.client.SoapCxfClient.MESSAGE_DISPATCHER;
import static org.mule.service.soap.client.SoapCxfClient.MULE_SOAP_ACTION;
import static org.mule.service.soap.client.SoapCxfClient.MULE_SOAP_OPERATION_STYLE;
import static org.mule.service.soap.client.SoapCxfClient.MULE_TRANSPORT_HEADERS_KEY;
import static org.mule.service.soap.client.SoapCxfClient.MULE_WSC_ADDRESS;
import static org.mule.service.soap.interceptor.SoapActionInterceptor.SOAP_ACTION;

import org.mule.runtime.extension.api.soap.message.DispatchingRequest;
import org.mule.runtime.extension.api.soap.message.DispatchingResponse;
import org.mule.runtime.extension.api.soap.message.MessageDispatcher;
import org.mule.runtime.soap.api.client.SoapClientConfiguration;
import org.mule.wsdl.parser.model.operation.OperationType;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.cxf.endpoint.ClientImpl;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.transport.MessageObserver;

/**
 * CXF interceptor that uses a custom {@link MessageDispatcher}, specified in the {@link SoapClientConfiguration} to send a SOAP
 * message and inject the obtained response into the CXF <strong>in</strong> (response) interceptors lifecycle.
 *
 * @since 1.0
 */
public class MessageDispatcherInterceptor extends AbstractPhaseInterceptor<Message> {

  private final MessageObserver messageObserver;

  public MessageDispatcherInterceptor(MessageObserver messageObserver) {
    super(SEND_ENDING);
    this.messageObserver = messageObserver;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Intercepts the SOAP message and performs the dispatch of it, receiving the response and sending it to the IN intercepting
   * processor chain.
   */
  @Override
  public void handleMessage(Message message) throws Fault {
    Exchange exchange = message.getExchange();
    Object encoding = exchange.get(ENCODING);
    message.put(ENCODING, encoding);

    // Performs all the remaining interceptions before sending.
    message.getInterceptorChain().doIntercept(message);

    // Wipe the request attachment list, so don't get mixed with the response ones.
    message.setAttachments(emptyList());

    MessageDispatcher dispatcher = (MessageDispatcher) exchange.get(MESSAGE_DISPATCHER);
    DispatchingResponse response = dispatcher.dispatch(DispatchingRequestFactory.createDispatchingRequest(message));

    // This needs to be set because we want the wsc closes the final stream,
    // otherwise cxf will close it too early when handling message in the StaxInEndingInterceptor.
    exchange.put(STAX_IN_NOCLOSE, TRUE);

    if (OperationType.ONE_WAY.equals(exchange.get(MULE_SOAP_OPERATION_STYLE))) {
      exchange.put(ClientImpl.FINISHED, true);
    } else {
      handleRequestResponse(exchange, encoding, response);
    }
  }

  private void handleRequestResponse(Exchange exchange, Object encoding, DispatchingResponse response) {
    Message inMessage = new MessageImpl();
    inMessage.put(ENCODING, encoding);
    inMessage.put(CONTENT_TYPE, response.getContentType());
    inMessage.setContent(InputStream.class, response.getContent());
    exchange.put(MULE_TRANSPORT_HEADERS_KEY, response.getHeaders());
    inMessage.setExchange(exchange);
    messageObserver.onMessage(inMessage);
  }
}
