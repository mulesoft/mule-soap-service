/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.generator.attachment;

import static org.mule.metadata.api.utils.MetadataTypeUtils.getLocalPart;

import org.mule.metadata.api.TypeLoader;
import org.mule.metadata.api.model.ObjectFieldType;
import org.mule.service.soap.interceptor.OutputMtomSoapAttachmentsInterceptor;
import org.mule.wsdl.parser.model.operation.OperationModel;

import java.util.List;
import java.util.Map;

import org.apache.cxf.message.Exchange;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * {@link AttachmentResponseEnricher} implementation for clients that works with MTOM.
 *
 * @since 1.0
 */
public final class MtomResponseEnricher extends AttachmentResponseEnricher {

  public MtomResponseEnricher(TypeLoader loader, Map<String, OperationModel> operations) {
    super(loader, operations);
  }

  /**
   * {@inheritDoc}
   * <p>
   * Removes the attachments nodes from the response that have been already processed by the
   * {@link OutputMtomSoapAttachmentsInterceptor}
   */
  @Override
  protected void processResponseAttachments(Document response, List<ObjectFieldType> attachments, Exchange exchange) {
    attachments.forEach(a -> {
      String tagName = getLocalPart(a);
      Node attachmentNode = response.getDocumentElement().getElementsByTagName(tagName).item(0);
      response.getDocumentElement().removeChild(attachmentNode);
    });
  }
}
