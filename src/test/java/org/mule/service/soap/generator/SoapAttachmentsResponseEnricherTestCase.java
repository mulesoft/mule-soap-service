/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.generator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mule.service.soap.SoapTestUtils.assertSimilarXml;
import static org.mule.service.soap.SoapTestXmlValues.DOWNLOAD_ATTACHMENT;
import static org.mule.service.soap.client.SoapCxfClient.MULE_ATTACHMENTS_KEY;
import static org.mule.service.soap.util.XmlTransformationUtils.stringToDocument;

import org.mule.metadata.xml.api.XmlTypeLoader;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.extension.api.soap.SoapAttachment;
import org.mule.service.soap.generator.attachment.AttachmentResponseEnricher;
import org.mule.service.soap.generator.attachment.SoapAttachmentResponseEnricher;
import org.mule.wsdl.parser.model.operation.OperationModel;

import java.util.Map;

import io.qameta.allure.Description;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

public class SoapAttachmentsResponseEnricherTestCase extends AbstractEnricherTestCase {

  private AttachmentResponseEnricher enricher;

  @Before
  public void setup() {
    super.setup();
    Map<String, OperationModel> ops = model.getService("TestService").getPort("TestPort").getOperationsMap();
    enricher = new SoapAttachmentResponseEnricher(model.getLoader().getValue(), ops);
  }

  @Test
  @Description("Enrich a response that contains attachments encoded with windows-1252")
  public void enrichWindows1252EncodingAttachment() throws Exception {
    String response =
        "<con:downloadAttachmentResponse xmlns:con=\"http://service.soap.service.mule.org/\">"
            + "<attachment>QWxndW0gQ29udGX6ZG8gZW0gUG9ydHVndepzIHBhcmEgV2luZG93cw==</attachment>"
            + "</con:downloadAttachmentResponse>";

    ExchangeImpl exchange = new ExchangeImpl();
    Document doc = stringToDocument(response);

    String result = enricher.enrich(doc, DOWNLOAD_ATTACHMENT, exchange);

    assertSimilarXml(testValues.getDownloadAttachmentResponse(), result);
    assertAttachment(exchange, "Algum Conteúdo em Português para Windows", "windows-1252");
  }

  @Test
  @Description("Enrich a response that contains attachments encoded with UTF-8")
  public void enrichUtf8EncodingAttachment() throws Exception {
    String response =
        "<con:downloadAttachmentResponse xmlns:con=\"http://service.soap.service.mule.org/\">"
            + "<attachment>U29tZSBDb250ZW50</attachment>"
            + "</con:downloadAttachmentResponse>";

    ExchangeImpl exchange = new ExchangeImpl();
    Document doc = stringToDocument(response);
    Map<String, OperationModel> ops = model.getService("TestService").getPort("TestPort").getOperationsMap();
    enricher = new SoapAttachmentResponseEnricher(model.getLoader().getValue(), ops);

    String result = enricher.enrich(doc, DOWNLOAD_ATTACHMENT, exchange);

    assertSimilarXml(testValues.getDownloadAttachmentResponse(), result);
    assertAttachment(exchange, "Some Content", "UTF-8");
  }

  private void assertAttachment(Exchange exchange, String attachmentText, String attachmentEncoding) {
    Map<String, SoapAttachment> attachments = (Map<String, SoapAttachment>) exchange.get(MULE_ATTACHMENTS_KEY);
    assertThat(attachments.entrySet(), hasSize(1));
    String value = IOUtils.toString(attachments.get("attachment").getContent(), attachmentEncoding);
    assertThat(value, is(attachmentText));
  }
}
