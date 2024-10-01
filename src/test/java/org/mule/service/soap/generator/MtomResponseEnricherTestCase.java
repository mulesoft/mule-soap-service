/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.generator;

import static org.mule.service.soap.SoapTestUtils.assertSimilarXml;
import static org.mule.service.soap.SoapTestXmlValues.DOWNLOAD_ATTACHMENT;
import static org.mule.service.soap.util.XmlTransformationUtils.stringToDocument;

import org.mule.service.soap.generator.attachment.AttachmentResponseEnricher;
import org.mule.service.soap.generator.attachment.MtomResponseEnricher;
import org.mule.wsdl.parser.model.operation.OperationModel;

import java.util.Map;

import io.qameta.allure.Description;
import org.apache.cxf.message.ExchangeImpl;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

public class MtomResponseEnricherTestCase extends AbstractEnricherTestCase {

  private AttachmentResponseEnricher enricher;

  @Before
  public void setup() {
    super.setup();
    Map<String, OperationModel> ops = model.getService("TestService").getPort("TestPort").getOperationsMap();
    enricher = new MtomResponseEnricher(model.getLoader().getValue(), ops);
  }

  @Test
  @Description("Enrich a response that contains attachments")
  public void enrich() throws Exception {
    String response =
        "<con:downloadAttachmentResponse xmlns:con=\"http://service.soap.service.mule.org/\">"
            + "<attachment>"
            + "<xop:Include xmlns:xop=\"http://www.w3.org/2004/08/xop/include\" href=\"cid:attachment-id\"/>"
            + "</attachment>"
            + "</con:downloadAttachmentResponse>";

    ExchangeImpl exchange = new ExchangeImpl();
    Document doc = stringToDocument(response);

    String result = enricher.enrich(doc, DOWNLOAD_ATTACHMENT, exchange);

    assertSimilarXml(testValues.getDownloadAttachmentResponse(), result);
  }
}
