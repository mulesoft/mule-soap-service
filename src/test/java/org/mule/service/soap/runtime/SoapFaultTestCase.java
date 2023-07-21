/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.runtime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.mule.runtime.soap.api.message.SoapRequest.builder;
import static org.mule.service.soap.SoapTestXmlValues.FAIL;
import org.mule.runtime.soap.api.exception.BadRequestException;
import org.mule.runtime.soap.api.exception.SoapFaultException;
import org.mule.service.soap.AbstractSoapServiceTestCase;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Stories;
import io.qameta.allure.Story;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@Feature("SOAP SERVICE")
@Stories({@Story("Operation Execution"), @Story("Soap Fault")})
public class SoapFaultTestCase extends AbstractSoapServiceTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  @Description("Consumes an operation that throws a SOAP Fault and expects a Soap Fault Exception")
  public void failOperation() throws Exception {
    try {
      client.consume(builder().content(testValues.getFailRequest()).operation(FAIL).build());
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
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("The provided operation [FAIL] does not exist in the WSDL file");
    client.consume(builder().content(testValues.buildXml("FAIL", "")).operation("FAIL").build());
  }

  @Test
  @Description("Consumes an operation with a body that is not a valid XML")
  public void echoBodyIsNotValidXml() throws Exception {
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Error consuming the operation [echo], the request body is not a valid XML");
    client.consume(builder().operation("echo").content("Invalid Test Payload: this is not an XML").build());
  }
}
