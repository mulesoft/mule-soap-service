/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.generator.attachment;

import static org.mule.service.soap.xml.util.XMLUtils.toXml;

import org.mule.metadata.api.TypeLoader;
import org.mule.runtime.extension.api.soap.SoapAttachment;
import org.mule.runtime.soap.api.exception.SoapServiceException;
import org.mule.service.soap.util.XmlTransformationException;
import org.mule.service.soap.util.XmlTransformationUtils;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Abstract implementation for a request enricher that adds a node for each sent attachment to the incoming SOAP request with all
 * the information required to send the attachments using the SOAP protocol.
 *
 * @since 1.0
 */
public abstract class AttachmentRequestEnricher {

  protected TypeLoader loader;

  AttachmentRequestEnricher(TypeLoader loader) {
    this.loader = loader;
  }

  /**
   * @param body        the XML SOAP body provided by the user.
   * @param attachments the attachments to upload.
   */
  public String enrichRequest(String body, Map<String, SoapAttachment> attachments) {
    try {
      Document bodyDocument = XmlTransformationUtils.stringToDocument(body);
      Element documentElement = bodyDocument.getDocumentElement();
      attachments.forEach((name, attachment) -> {
        Element attachmentElement = bodyDocument.createElement(name);
        addAttachmentElement(bodyDocument, name, attachment, attachmentElement);
        documentElement.appendChild(attachmentElement);
      });
      return toXml(bodyDocument);
    } catch (XmlTransformationException e) {
      throw new SoapServiceException("Error while preparing request for the provided body", e);
    }
  }

  /**
   * Adds the content to the attachment node recently created to the XML SOAP request
   *
   * @param bodyDocument      the document where we are adding the node element.
   * @param attachment        the attachment to be sent.
   * @param attachmentElement the recently created attachment node in the xml request.
   */
  abstract void addAttachmentElement(Document bodyDocument, String name, SoapAttachment attachment, Element attachmentElement);

}
