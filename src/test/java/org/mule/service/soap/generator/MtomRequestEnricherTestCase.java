/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.generator;

import static org.mule.test.allure.AllureConstants.WscFeature.WSC_EXTENSION;

import io.qameta.allure.Stories;
import org.mule.service.soap.generator.attachment.AttachmentRequestEnricher;
import org.mule.service.soap.generator.attachment.MtomRequestEnricher;

import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;

@Feature(WSC_EXTENSION)
@Stories({@Story("Attachments"), @Story("MTOM"), @Story("Request Generation")})
public class MtomRequestEnricherTestCase extends AbstractRequestEnricherTestCase {

  @Override
  @Step("Returns an MTOM enricher that adds an XOP element to the XML referencing the attachment in the multipart message")
  protected AttachmentRequestEnricher getEnricher() {
    return new MtomRequestEnricher(definition, loader);
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
