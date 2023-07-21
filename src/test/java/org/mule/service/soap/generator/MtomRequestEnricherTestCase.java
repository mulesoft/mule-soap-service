/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.generator;

import static org.mule.test.allure.AllureConstants.WscFeature.WSC_EXTENSION;

import org.mule.service.soap.generator.attachment.AttachmentRequestEnricher;
import org.mule.service.soap.generator.attachment.MtomRequestEnricher;

import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Stories;
import io.qameta.allure.Story;

@Feature(WSC_EXTENSION)
@Stories({@Story("Attachments"), @Story("MTOM"), @Story("Request Generation")})
public class MtomRequestEnricherTestCase extends AbstractRequestEnricherTestCase {

  @Override
  @Step("Returns an MTOM enricher that adds an XOP element to the XML referencing the attachment in the multipart message")
  protected AttachmentRequestEnricher getEnricher() {
    return new MtomRequestEnricher(model.getLoader().getValue());
  }

  @Override
  protected String getExpectedResult() {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<con:uploadAttachment xmlns:con=\"http://service.soap.service.mule.org/\">"
        + "<attachment-id>"
        + "<xop:Include xmlns:xop=\"http://www.w3.org/2004/08/xop/include\" href=\"cid:attachment-id\"/>"
        + "</attachment-id>"
        + "</con:uploadAttachment>";
  }
}
