/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.generator;


import static org.mule.test.allure.AllureConstants.WscFeature.WSC_EXTENSION;

import org.mule.service.soap.generator.attachment.AttachmentRequestEnricher;
import org.mule.service.soap.generator.attachment.SoapAttachmentRequestEnricher;

import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Stories;
import io.qameta.allure.Story;

@Feature(WSC_EXTENSION)
@Stories({@Story("Attachments"), @Story("Request Generation")})
public class SoapAttachmentsRequestEnricherTestCase extends AbstractRequestEnricherTestCase {

  @Override
  @Step("Returns an attachment enricher that adds the content of the attachment encoded to base64")
  protected AttachmentRequestEnricher getEnricher() {
    return new SoapAttachmentRequestEnricher(model.getLoader().getValue());
  }

  @Override
  protected String getExpectedResult() {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<con:uploadAttachment xmlns:con=\"http://service.soap.service.mule.org/\">"
        + "<attachment-id>U29tZSBDb250ZW50</attachment-id>"
        + "</con:uploadAttachment>";
  }
}
