/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.generator;

import static org.apache.commons.lang3.StringUtils.isBlank;

import org.mule.metadata.api.TypeLoader;
import org.mule.runtime.extension.api.soap.SoapAttachment;
import org.mule.runtime.soap.api.exception.SoapServiceException;
import org.mule.service.soap.generator.attachment.AttachmentRequestEnricher;
import org.mule.service.soap.util.XmlTransformationException;
import org.mule.service.soap.util.XmlTransformationUtils;
import org.mule.wsdl.parser.model.PortModel;

import java.util.Map;

import javax.xml.stream.XMLStreamReader;

/**
 * Generates a XML SOAP request used to invoke CXF.
 * <p>
 * If no body is provided will try to generate a default one.
 * <p>
 * for each attachment will add a node with the required information depending on the protocol that it's being used.
 *
 * @since 1.0
 */
public final class SoapRequestGenerator {

  private final EmptyRequestGenerator emptyRequestGenerator;
  private final AttachmentRequestEnricher requestEnricher;

  public SoapRequestGenerator(AttachmentRequestEnricher requestEnricher, PortModel port, TypeLoader loader) {
    this.requestEnricher = requestEnricher;
    this.emptyRequestGenerator = new EmptyRequestGenerator(port, loader);
  }

  /**
   * Generates an {@link XMLStreamReader} SOAP request ready to be consumed by CXF.
   * 
   * @param operation   the name of the operation being invoked.
   * @param body        the body content provided by the user.
   * @param attachments the attachments provided by the user.
   */
  public XMLStreamReader generate(String operation, String body, Map<String, SoapAttachment> attachments) {

    if (isBlank(body)) {
      body = emptyRequestGenerator.generateRequest(operation);
    }

    if (!attachments.isEmpty()) {
      body = requestEnricher.enrichRequest(body, attachments);
    }

    try {
      return XmlTransformationUtils.stringToXmlStreamReader(body);
    } catch (XmlTransformationException e) {
      throw new SoapServiceException("Error generating SOAP request", e);
    }
  }
}
