/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.generator;

import static org.mule.service.soap.SoapTestUtils.assertSimilarXml;
import static org.mule.service.soap.SoapTestXmlValues.ECHO;
import static org.mule.service.soap.SoapTestXmlValues.NO_PARAMS;
import static org.mule.test.allure.AllureConstants.WscFeature.WSC_EXTENSION;

import org.mule.runtime.soap.api.exception.BadRequestException;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@Feature(WSC_EXTENSION)
@Story("Request Generation")
public class EmptyRequestGeneratorTestCase extends AbstractEnricherTestCase {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  private EmptyRequestGenerator generator;

  @Before
  public void setup() {
    super.setup();
    generator = new EmptyRequestGenerator(model.getService("TestService").getPort("TestPort"), model.getLoader().getValue());
  }

  @Test
  @Description("Checks the generation of a body request for an operation that don't require any parameters")
  public void noParams() throws Exception {
    String request = generator.generateRequest(NO_PARAMS);
    assertSimilarXml(request, testValues.getNoParamsRequest());
  }

  @Test
  @Description("Checks that the generation of a body request for an operation that require parameters fails")
  public void withParams() throws Exception {
    exception.expect(BadRequestException.class);
    exception
        .expectMessage("Cannot build default body request for operation [echo], the operation requires input parameters");
    generator.generateRequest(ECHO);
  }
}
