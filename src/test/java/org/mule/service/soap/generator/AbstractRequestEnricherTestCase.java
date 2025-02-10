/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.generator;

import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.extension.api.soap.SoapAttachment;
import org.mule.service.soap.SoapTestUtils;
import org.mule.service.soap.generator.attachment.AttachmentRequestEnricher;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import io.qameta.allure.Description;
import io.qameta.allure.Step;

public abstract class AbstractRequestEnricherTestCase extends AbstractEnricherTestCase {

  @Test
  @Description("Enrich a request that contains attachments")
  public void enrich() throws Exception {
    SoapAttachment attachment = getTestAttachment();
    AttachmentRequestEnricher enricher = getEnricher();
    String request = enricher.enrichRequest(testValues.getUploadAttachmentRequest(), singletonMap("attachment-id", attachment));
    SoapTestUtils.assertSimilarXml(getExpectedResult(), request);
  }

  @Step("Prepares a test attachment")
  private SoapAttachment getTestAttachment() {
    SoapAttachment attachment = mock(SoapAttachment.class);
    when(attachment.getContent()).thenReturn(IOUtils.toInputStream("Some Content"));
    when(attachment.getContentType()).thenReturn(MediaType.TEXT);
    return attachment;
  }

  protected abstract AttachmentRequestEnricher getEnricher();

  protected abstract String getExpectedResult();
}
