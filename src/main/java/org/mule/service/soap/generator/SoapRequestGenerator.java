/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.generator;

import static org.apache.commons.lang3.StringUtils.isBlank;
import org.mule.metadata.xml.XmlTypeLoader;
import org.mule.runtime.extension.api.soap.SoapAttachment;
import org.mule.runtime.soap.api.exception.SoapServiceException;
import org.mule.service.soap.generator.attachment.AttachmentRequestEnricher;
import org.mule.service.soap.introspection.WsdlDefinition;
import org.mule.service.soap.util.XmlTransformationException;
import org.mule.service.soap.util.XmlTransformationUtils;

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

  public SoapRequestGenerator(AttachmentRequestEnricher requestEnricher, WsdlDefinition definition, XmlTypeLoader loader) {
    this.requestEnricher = requestEnricher;
    this.emptyRequestGenerator = new EmptyRequestGenerator(definition, loader);
  }

  /**
   * Generates an {@link XMLStreamReader} SOAP request ready to be consumed by CXF.
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
