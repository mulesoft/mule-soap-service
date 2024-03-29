/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.generator;

import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.util.Collections.emptyMap;
import static org.mule.runtime.api.metadata.MediaType.APPLICATION_XML;
import static org.mule.service.soap.client.SoapCxfClient.MULE_ATTACHMENTS_KEY;
import static org.mule.service.soap.client.SoapCxfClient.MULE_HEADERS_KEY;
import static org.mule.service.soap.client.SoapCxfClient.MULE_TRANSPORT_HEADERS_KEY;

import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.soap.SoapAttachment;
import org.mule.runtime.extension.api.soap.SoapAttributes;
import org.mule.runtime.soap.api.client.SoapClient;
import org.mule.runtime.soap.api.exception.BadResponseException;
import org.mule.runtime.soap.api.message.SoapRequest;
import org.mule.runtime.soap.api.message.SoapResponse;
import org.mule.service.soap.generator.attachment.AttachmentResponseEnricher;
import org.mule.service.soap.message.EmptySoapResponse;
import org.mule.service.soap.message.ImmutableSoapResponse;
import org.mule.service.soap.util.XmlTransformationException;
import org.mule.service.soap.util.XmlTransformationUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.message.Exchange;
import org.w3c.dom.Document;

/**
 * Class used to generate the output of the {@link SoapClient#consume(SoapRequest)} using the CXF response.
 *
 * @since 1.0
 */
public final class SoapResponseGenerator {

  private final AttachmentResponseEnricher responseEnricher;

  public SoapResponseGenerator(AttachmentResponseEnricher responseEnricher) {
    this.responseEnricher = responseEnricher;
  }

  /**
   * Generates an {@link Result} with the out attachments and headers and the response body of the SOAP operation.
   * <p>
   * If there are out attachments the nodes in the response associated to them will be removed so the end user don't need to
   * handle those nodes.
   * <p>
   * The our SOAP headers and the protocol specific headers will be retrieved in a {@link SoapAttributes} as attributes in the
   * returned {@link Result}.
   *
   * @param operation the name of the operation that was invoked
   * @param response  the CXF response
   * @param exchange  the exchange used for CXF to store the headers and attachments.
   */
  public SoapResponse generate(String operation, Object[] response, Exchange exchange) {
    Map<String, String> transportHeaders = getTransportHeaders(exchange);
    if (response == null) {
      return new EmptySoapResponse(transportHeaders);
    }

    Document document = unwrapResponse(response);
    String result = responseEnricher.enrich(document, operation, exchange);
    Map<String, SoapAttachment> attachments = (Map<String, SoapAttachment>) exchange.get(MULE_ATTACHMENTS_KEY);
    Map<String, String> headers = (Map<String, String>) exchange.get(MULE_HEADERS_KEY);
    ByteArrayInputStream resultStream = new ByteArrayInputStream(result.getBytes());
    MediaType mediaType = getContentType(transportHeaders);
    return new ImmutableSoapResponse(resultStream, headers, transportHeaders, attachments, mediaType);
  }

  private MediaType getContentType(Map<String, String> transportHeaders) {
    String ct = transportHeaders.get("content-type");
    if (ct != null) {
      Optional<Charset> charset = MediaType.parse(ct).getCharset();
      if (charset.isPresent()) {
        return MediaType.create("application", "xml", charset.get());
      }
    }
    return APPLICATION_XML;
  }

  private Map<String, String> getTransportHeaders(Exchange exchange) {
    Map<String, String> transportHeaders = (Map<String, String>) exchange.get(MULE_TRANSPORT_HEADERS_KEY);
    if (transportHeaders == null) {
      return emptyMap();
    } else {
      Map<String, String> result = new TreeMap<>(CASE_INSENSITIVE_ORDER);
      result.putAll(transportHeaders);
      return result;
    }
  }

  /**
   * Unwraps the CXF {@link XMLStreamReader} response into a dom {@link Document}.
   *
   * @param response the CXF received response.
   */
  private Document unwrapResponse(Object[] response) {
    if (response.length == 0) {
      throw new BadResponseException("no elements were received in the SOAP response.");
    }
    if (response.length != 1) {
      throw new BadResponseException("the obtained response contains more than one element, only one was expected");
    }
    XMLStreamReader reader = (XMLStreamReader) response[0];
    try {
      return XmlTransformationUtils.xmlStreamReaderToDocument(reader);
    } catch (XmlTransformationException e) {
      throw new BadResponseException("Error transforming the XML web service response to be processed", e);
    }
  }
}
