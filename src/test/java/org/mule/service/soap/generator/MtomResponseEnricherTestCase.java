/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.generator;

import org.mule.metadata.xml.api.XmlTypeLoader;
import org.mule.service.soap.generator.attachment.AttachmentResponseEnricher;
import org.mule.service.soap.generator.attachment.MtomResponseEnricher;
import org.mule.wsdl.parser.model.operation.OperationModel;

import java.util.Map;

import org.apache.cxf.message.Exchange;

public class MtomResponseEnricherTestCase extends ResponseEnricherTestCase {

  private static final String RESPONSE =
      "<con:downloadAttachmentResponse xmlns:con=\"http://service.soap.service.mule.org/\">"
          + "<attachment>"
          + "<xop:Include xmlns:xop=\"http://www.w3.org/2004/08/xop/include\" href=\"cid:attachment-id\"/>"
          + "</attachment>"
          + "</con:downloadAttachmentResponse>";

  @Override
  protected AttachmentResponseEnricher getEnricher(XmlTypeLoader loader, Map<String, OperationModel> ops) {
    return new MtomResponseEnricher(loader, ops);
  }

  @Override
  protected String getResponse() {
    return RESPONSE;
  }

  @Override
  protected void assertAttachment(Exchange exchange) {
    // Attachments are handled by the Mtom Interceptor
  }
}
