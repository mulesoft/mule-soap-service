/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.generator;

import static java.util.Collections.emptyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.service.soap.SoapTestUtils.assertSimilarXml;
import static org.mule.service.soap.SoapTestXmlValues.ECHO;
import static org.mule.service.soap.SoapTestXmlValues.FAIL;
import static org.mule.service.soap.SoapTestXmlValues.NO_PARAMS;
import static org.mule.test.allure.AllureConstants.WscFeature.WSC_EXTENSION;

import org.mule.runtime.soap.api.exception.BadRequestException;
import org.mule.runtime.soap.api.exception.InvalidWsdlException;
import org.mule.service.soap.introspection.WsdlDefinition;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import ru.yandex.qatools.allure.annotations.Description;
import ru.yandex.qatools.allure.annotations.Features;
import ru.yandex.qatools.allure.annotations.Stories;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;

@Features(WSC_EXTENSION)
@Stories("Request Generation")
public class EmptyRequestGeneratorTestCase extends AbstractEnricherTestCase {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  private EmptyRequestGenerator generator;

  @Before
  public void setup() {
    super.setup();
    generator = new EmptyRequestGenerator(definition, loader);
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
        .expectMessage("Cannot buildOutputType default body request for operation [echo], the operation requires input parameters");
    generator.generateRequest(ECHO);
  }

  @Test
  @Description("Checks that the generation of a body request for an operation without a body part fails")
  public void noBodyPart() throws Exception {
    exception.expect(InvalidWsdlException.class);
    exception.expectMessage("No SOAP body defined in the WSDL for the specified operation");

    // Makes that the definition returns an Binding Operation without input SOAP body.
    WsdlDefinition definition = mock(WsdlDefinition.class);
    BindingOperation bop = mock(BindingOperation.class);
    BindingInput bi = mock(BindingInput.class);
    when(bi.getExtensibilityElements()).thenReturn(emptyList());
    when(bop.getBindingInput()).thenReturn(bi);
    when(definition.getBindingOperation(anyString())).thenReturn(bop);

    EmptyRequestGenerator emptyRequestGenerator = new EmptyRequestGenerator(definition, loader);
    emptyRequestGenerator.generateRequest(FAIL);
  }
}
