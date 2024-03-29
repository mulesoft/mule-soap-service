/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.generator.attachment;


import static java.lang.String.format;

import org.mule.metadata.api.TypeLoader;
import org.mule.runtime.extension.api.soap.SoapAttachment;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * {@link AttachmentRequestEnricher} implementation for clients that works with MTOM.
 *
 * @since 1.0
 */
public final class MtomRequestEnricher extends AttachmentRequestEnricher {

  private static final String XOP_NS = "http://www.w3.org/2004/08/xop/include";
  private static final String XOP_TAG_NAME = "xop:Include";
  private static final String HREF = "href";
  private static final String CONTENT_ID_MASK = "cid:%s";

  public MtomRequestEnricher(TypeLoader loader) {
    super(loader);
  }


  /**
   * {@inheritDoc}
   * <p>
   * Adds the XOP element to the attachment node this way the attachments gets tracked with its CID (content id) when it's sent in
   * the multipart request.
   * <p>
   * Basically adds this content to the attachment node
   * {@code <xop:Include xmlns:xop="http://www.w3.org/2004/08/xop/include" href="cid:attachmentContentId"/>} and uses the <strong>
   * attachmentContentId</strong> to identify the attachment in the multipart.
   */
  @Override
  protected void addAttachmentElement(Document bodyDocument, String name, SoapAttachment attachment, Element attachmentNode) {
    Element xop = bodyDocument.createElementNS(XOP_NS, XOP_TAG_NAME);
    // sets the content id of the attachment that is going to be sent in the multipart message.
    xop.setAttribute(HREF, format(CONTENT_ID_MASK, name));
    attachmentNode.appendChild(xop);
  }
}
