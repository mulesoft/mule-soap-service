/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.runtime.attachments;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.api.metadata.MediaType.HTML;
import static org.mule.runtime.soap.api.message.SoapRequest.builder;
import static org.mule.service.soap.SoapTestUtils.assertSimilarXml;
import static org.mule.service.soap.SoapTestXmlValues.DOWNLOAD_ATTACHMENT;
import static org.mule.service.soap.SoapTestXmlValues.UPLOAD_ATTACHMENT;

import org.mule.runtime.extension.api.soap.SoapAttachment;
import org.mule.runtime.soap.api.message.SoapRequest;
import org.mule.runtime.soap.api.message.SoapResponse;
import org.mule.service.soap.AbstractSoapServiceTestCase;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import io.qameta.allure.Description;
import java.io.ByteArrayInputStream;
import java.util.Map;

public class AttachmentsTestCase extends AbstractSoapServiceTestCase {

  @Test
  @Description("Downloads an attachment from a mtom server")
  public void downloadAttachment() throws Exception {
    SoapRequest req = builder().withContent(testValues.getDownloadAttachmentRequest()).withOperation(DOWNLOAD_ATTACHMENT).build();
    SoapResponse response = client.consume(req);
    assertSimilarXml(testValues.getDownloadAttachmentResponse(), response.getContent());
    Map<String, SoapAttachment> attachments = response.getAttachments();
    assertThat(attachments.entrySet(), hasSize(1));
    SoapAttachment attachment = attachments.entrySet().iterator().next().getValue();
    assertThat(IOUtils.toString(attachment.getContent()), containsString("Simple Attachment Content"));
  }

  @Test
  @Description("Uploads an attachment to a mtom server")
  public void uploadAttachment() throws Exception {
    SoapRequest request = builder()
        .withAttachment("attachment", new SoapAttachment(new ByteArrayInputStream("Some Content".getBytes()), HTML))
        .withContent(testValues.getUploadAttachmentRequest())
        .withOperation(UPLOAD_ATTACHMENT)
        .build();
    SoapResponse response = client.consume(request);
    assertSimilarXml(testValues.getUploadAttachmentResponse(), response.getContent());
  }
}
