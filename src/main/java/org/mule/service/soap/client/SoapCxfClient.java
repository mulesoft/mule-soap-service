/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.client;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.cxf.message.Message.ENCODING;
import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;
import static org.mule.runtime.core.api.util.IOUtils.toDataHandler;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_DISPOSITION;
import static org.mule.service.soap.util.XmlTransformationUtils.stringToDomElement;

import org.mule.metadata.api.TypeLoader;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.extension.api.soap.SoapAttachment;
import org.mule.runtime.extension.api.soap.message.MessageDispatcher;
import org.mule.runtime.soap.api.SoapVersion;
import org.mule.runtime.soap.api.client.SoapClient;
import org.mule.runtime.soap.api.client.metadata.SoapMetadataResolver;
import org.mule.runtime.soap.api.exception.BadRequestException;
import org.mule.runtime.soap.api.exception.DispatchingException;
import org.mule.runtime.soap.api.exception.SoapFaultException;
import org.mule.runtime.soap.api.exception.SoapServiceException;
import org.mule.runtime.soap.api.message.SoapRequest;
import org.mule.runtime.soap.api.message.SoapResponse;
import org.mule.service.soap.generator.SoapRequestGenerator;
import org.mule.service.soap.generator.SoapResponseGenerator;
import org.mule.service.soap.generator.attachment.AttachmentRequestEnricher;
import org.mule.service.soap.generator.attachment.AttachmentResponseEnricher;
import org.mule.service.soap.generator.attachment.MtomRequestEnricher;
import org.mule.service.soap.generator.attachment.MtomResponseEnricher;
import org.mule.service.soap.generator.attachment.SoapAttachmentRequestEnricher;
import org.mule.service.soap.generator.attachment.SoapAttachmentResponseEnricher;
import org.mule.service.soap.metadata.DefaultSoapMetadataResolver;
import org.mule.service.soap.util.XmlTransformationException;
import org.mule.service.soap.util.XmlTransformationUtils;
import org.mule.wsdl.parser.exception.OperationNotFoundException;
import org.mule.wsdl.parser.model.PortModel;
import org.mule.wsdl.parser.model.WsdlModel;
import org.mule.wsdl.parser.model.operation.OperationModel;

import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.attachment.AttachmentImpl;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapHeader;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * a {@link SoapClient} implementation based on CXF.
 *
 * @since 1.0
 */
public class SoapCxfClient implements SoapClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(SoapCxfClient.class);

  public static final String MESSAGE_DISPATCHER = "mule.soap.dispatcher";
  public static final String MULE_ATTACHMENTS_KEY = "mule.soap.attachments";
  public static final String MULE_WSC_ADDRESS = "mule.soap.address";
  public static final String MULE_HEADERS_KEY = "mule.soap.headers";
  public static final String MULE_TRANSPORT_HEADERS_KEY = "mule.soap.transport.headers";
  public static final String MULE_SOAP_ACTION = "mule.soap.action";
  public static final String MULE_SOAP_OPERATION_STYLE = "mule.soap.operation.type";

  private final SoapRequestGenerator requestGenerator;
  private final SoapResponseGenerator responseGenerator;

  private final Client client;
  private final WsdlModel wsdlModel;
  private final PortModel port;
  private final TypeLoader loader;
  private final String address;
  private final MessageDispatcher defaultDispatcher;
  private final SoapVersion version;
  private final String encoding;
  private final boolean isMtom;

  SoapCxfClient(Client client,
                WsdlModel wsdlModel,
                PortModel portModel,
                String address,
                MessageDispatcher dispatcher,
                SoapVersion version,
                String encoding,
                boolean isMtom) {
    this.client = client;
    this.wsdlModel = wsdlModel;
    this.port = portModel;
    this.loader = wsdlModel.getLoader().getValue();
    this.address = address;
    this.defaultDispatcher = dispatcher;
    this.version = version;
    this.isMtom = isMtom;
    this.encoding = encoding;
    // TODO: MULE-10889 -> instead of creating this enrichers, interceptors that works with the live stream would be ideal
    this.requestGenerator = new SoapRequestGenerator(getRequestEnricher(isMtom), portModel, loader);
    this.responseGenerator = new SoapResponseGenerator(getResponseEnricher(isMtom));
  }

  @Override
  public SoapResponse consume(SoapRequest request) {
    return consume(request, defaultDispatcher);
  }

  @Override
  public SoapResponse consume(SoapRequest request, MessageDispatcher dispatcher) {
    requireNonNull(dispatcher, "Message Dispatcher cannot be null");
    String operation = request.getOperation();
    Exchange exchange = new ExchangeImpl();
    Object[] response = invoke(request, exchange, dispatcher);
    return responseGenerator.generate(operation, response, exchange);
  }

  @Override
  public void stop() throws MuleException {
    disposeIfNeeded(defaultDispatcher, LOGGER);
    stopIfNeeded(defaultDispatcher);
    client.destroy();
  }

  @Override
  public void start() throws MuleException {
    initialiseIfNeeded(defaultDispatcher);
    startIfNeeded(defaultDispatcher);
  }

  @Override
  public SoapMetadataResolver getMetadataResolver() {
    return new DefaultSoapMetadataResolver(wsdlModel, port, loader);
  }

  private Object[] invoke(SoapRequest request, Exchange exchange, MessageDispatcher dispatcher) {
    String operation = request.getOperation();
    XMLStreamReader xmlBody = getXmlBody(request);
    try {
      Map<String, Object> ctx = getInvocationContext(request, dispatcher);
      return client.invoke(getInvocationOperation(), new Object[] {xmlBody}, ctx, exchange);
    } catch (SoapFault sf) {
      throw new SoapFaultException(sf.getFaultCode(), sf.getSubCode(), parseExceptionDetail(sf.getDetail()).orElse(null),
                                   sf.getReason(), sf.getNode(), sf.getRole(), sf);
    } catch (Fault f) {
      if (f.getMessage().contains("COULD_NOT_READ_XML")) {
        throw new BadRequestException("Error consuming the operation [" + operation + "], the request body is not a valid XML");
      }
      throw new SoapFaultException(f.getFaultCode(), parseExceptionDetail(f.getDetail()).orElse(null), f);
    } catch (DispatchingException e) {
      throw e;
    } catch (OperationNotFoundException e) {
      String location = wsdlModel.getLocation();
      throw new BadRequestException("The provided operation [" + operation + "] does not exist in the WSDL file [" + location
          + "]", e);
    } catch (Exception e) {
      throw new SoapServiceException("Unexpected error while consuming the web service operation [" + operation + "]", e);
    }
  }

  private XMLStreamReader getXmlBody(SoapRequest request) {
    try {
      String xml = request.getContent() != null ? IOUtils.toString(request.getContent()) : null;
      return requestGenerator.generate(request.getOperation(), xml, request.getAttachments());
    } catch (IOException e) {
      throw new BadRequestException("an error occurred while parsing the provided request");
    }
  }

  private BindingOperationInfo getInvocationOperation() throws Exception {
    // Normally its not this hard to invoke the CXF Client, but we're
    // sending along some exchange properties, so we need to use a more advanced
    // method
    Endpoint ep = client.getEndpoint();
    // The operation is always named invoke because hits our ProxyService implementation.
    QName q = new QName(ep.getService().getName().getNamespaceURI(), "invoke");
    BindingOperationInfo bop = ep.getBinding().getBindingInfo().getOperation(q);
    if (bop.isUnwrappedCapable()) {
      bop = bop.getUnwrappedOperation();
    }
    return bop;
  }

  private Map<String, Object> getInvocationContext(SoapRequest request,
                                                   MessageDispatcher dispatcher) {
    Map<String, Object> props = new HashMap<>();
    OperationModel operation = port.getOperation(request.getOperation());

    // is NOT mtom the attachments must not be touched by cxf, we create a custom request embedding the attachment in the xml
    props.put(MULE_ATTACHMENTS_KEY, isMtom ? transformToCxfAttachments(request.getAttachments()) : emptyMap());
    props.put(MULE_WSC_ADDRESS, address);
    props.put(ENCODING, encoding == null ? "UTF-8" : encoding);
    props.put(MULE_HEADERS_KEY, transformToCxfHeaders(request.getSoapHeaders()));
    props.put(MULE_TRANSPORT_HEADERS_KEY, request.getTransportHeaders());
    props.put(MESSAGE_DISPATCHER, dispatcher);
    props.put(MULE_SOAP_OPERATION_STYLE, port.getOperation(request.getOperation()).getType());
    operation.getSoapAction().ifPresent(action -> props.put(MULE_SOAP_ACTION, action));
    Map<String, Object> ctx = new HashMap<>();
    ctx.put(Client.REQUEST_CONTEXT, props);
    return ctx;
  }

  private List<SoapHeader> transformToCxfHeaders(Map<String, String> headers) {
    if (headers == null) {
      return emptyList();
    }
    return headers.entrySet().stream()
        .map(header -> {
          try {
            return new SoapHeader(new QName(null, header.getKey()), stringToDomElement(header.getValue()));
          } catch (Exception e) {
            throw new BadRequestException("Cannot parse input header [" + header.getKey() + "]", e);
          }
        })
        .collect(toList());
  }

  private Map<String, Attachment> transformToCxfAttachments(Map<String, SoapAttachment> attachments) {
    ImmutableMap.Builder<String, Attachment> builder = ImmutableMap.builder();
    attachments.forEach((name, value) -> {
      try {
        AttachmentImpl attachment = new AttachmentImpl(name, toDataHandler(name, value.getContent(), value.getContentType()));
        if (isMtom) {
          attachment.setHeader(CONTENT_DISPOSITION, "attachment; name=\"" + name + "\"");
        }
        builder.put(name, attachment);
      } catch (IOException e) {
        throw new BadRequestException(format("Error while preparing attachment [%s] for upload", name), e);
      }
    });
    return builder.build();
  }

  private AttachmentRequestEnricher getRequestEnricher(boolean isMtom) {
    return isMtom ? new MtomRequestEnricher(loader) : new SoapAttachmentRequestEnricher(loader);
  }

  private AttachmentResponseEnricher getResponseEnricher(boolean isMtom) {
    return isMtom ? new MtomResponseEnricher(loader, port.getOperationsMap())
        : new SoapAttachmentResponseEnricher(loader, port.getOperationsMap());
  }

  private Optional<String> parseExceptionDetail(Element detail) {
    try {
      return ofNullable(XmlTransformationUtils.nodeToString(detail));
    } catch (XmlTransformationException e) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Error while parsing Soap Exception detail: " + detail.toString(), e);
      }
      return empty();
    }
  }

  @Override
  public String toString() {
    return reflectionToString(this);
  }
}
