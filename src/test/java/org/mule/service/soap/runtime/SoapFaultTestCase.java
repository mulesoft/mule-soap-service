/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.runtime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.mule.runtime.soap.api.SoapVersion.SOAP11;
import static org.mule.runtime.soap.api.message.SoapRequest.builder;
import static org.mule.service.soap.SoapTestXmlValues.FAIL;
import static org.mule.test.allure.AllureConstants.WscFeature.WSC_EXTENSION;

import io.qameta.allure.Stories;
import org.mule.runtime.soap.api.exception.BadRequestException;
import org.mule.runtime.soap.api.exception.SoapFaultException;
import org.mule.service.soap.AbstractSoapServiceTestCase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(WSC_EXTENSION)
@Stories({@Story("Operation Execution"), @Story("Soap Fault")})
public class SoapFaultTestCase extends AbstractSoapServiceTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  @Description("Consumes an operation that throws a SOAP Fault and expects a Soap Fault Exception")
  public void failOperation() throws Exception {
    try {
      client.consume(builder().withContent(testValues.getFailRequest()).withOperation(FAIL).build());
    } catch (SoapFaultException e) {
      // Server is for 1.1, Receiver for 1.2
      assertThat(e.getFaultCode().getLocalPart(), isOneOf("Server", "Receiver"));
      assertThat(e.getReason(), is("Fail Message"));
      assertThat(e.getDetail(), containsString("EchoException"));
      assertThat(e.getDetail(), containsString("Fail Message"));
    }
  }

  @Test
  @Description("Consumes an operation that does not exist and throws a SOAP Fault because of it and asserts the thrown exception")
  public void noExistentOperation() throws Exception {
    try {
      client.consume(builder().withContent(testValues.buildXml("INVALID", "")).withOperation("fail").build());
    } catch (SoapFaultException e) {
      // Client is for 1.1, Sender for 1.2
      assertThat(e.getFaultCode().getLocalPart(), isOneOf("Client", "Sender"));
      if (soapVersion.equals(SOAP11)) {
        assertThat(e.getReason(), containsString("{http://service.soap.service.mule.org/}INVALID was not recognized"));
      } else {
        assertThat(e.getReason(),
                   containsString("Unexpected wrapper element {http://service.soap.service.mule.org/}INVALID found."));
      }
    }
  }

  @Test
  @Description("Consumes an operation with a body that is not a valid XML")
  public void echoBodyIsNotValidXml() throws Exception {
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Error consuming the operation [echo], the request body is not a valid XML");
    client.consume(builder().withOperation("echo").withContent("Invalid Test Payload: this is not an XML").build());
  }
}
