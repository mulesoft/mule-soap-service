/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.generator.attachment;

import static java.lang.String.format;
import static org.mule.metadata.api.utils.MetadataTypeUtils.getLocalPart;
import static org.mule.runtime.api.metadata.MediaType.ANY;

import org.mule.metadata.api.TypeLoader;
import org.mule.metadata.api.model.ObjectFieldType;
import org.mule.runtime.core.api.transformer.TransformerException;
import org.mule.runtime.extension.api.soap.SoapAttachment;
import org.mule.runtime.soap.api.exception.EncodingException;
import org.mule.service.soap.client.SoapCxfClient;
import org.mule.wsdl.parser.model.operation.OperationModel;

import com.google.common.collect.ImmutableMap;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import org.apache.cxf.message.Exchange;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * {@link AttachmentResponseEnricher} implementation for SOAP with attachments.
 *
 * @since 1.0
 */
public final class SoapAttachmentResponseEnricher extends AttachmentResponseEnricher {

  private static final Base64Decoder decoder = new Base64Decoder();

  public SoapAttachmentResponseEnricher(TypeLoader loader, Map<String, OperationModel> operations) {
    super(loader, operations);
  }

  /**
   * {@inheritDoc}
   * <p>
   * Extracts the base64 encoded content from the attachment nodes, decodes them and then remove all the nodes to clean the
   * response body.
   */
  @Override
  protected void processResponseAttachments(Document response, List<ObjectFieldType> attachments, Exchange exchange) {
    ImmutableMap.Builder<String, SoapAttachment> builder = ImmutableMap.builder();
    attachments.forEach(attachment -> {
      String attachmentName = getLocalPart(attachment);
      builder.put(attachmentName, getAttachment(response, attachmentName));
    });
    exchange.put(SoapCxfClient.MULE_ATTACHMENTS_KEY, builder.build());
  }

  /**
   * Extracts the base64 encoded content from the attachment {@code name}, decodes its content and removes the node from the
   * document.
   */
  private SoapAttachment getAttachment(Document response, String name) {
    Node attachmentNode = response.getDocumentElement().getElementsByTagName(name).item(0);
    byte[] decodedAttachment = decodeAttachment(name, attachmentNode.getTextContent());
    response.getDocumentElement().removeChild(attachmentNode);
    return new SoapAttachment(new ByteArrayInputStream(decodedAttachment), ANY);
  }

  /**
   * Decodes the attachment content from base64.
   */
  private byte[] decodeAttachment(String name, String attachmentContent) {
    try {
      return (byte[]) decoder.transform(attachmentContent);
    } catch (TransformerException e) {
      throw new EncodingException(format("Cannot decode base64 attachment [%s]", name));
    }
  }
}
