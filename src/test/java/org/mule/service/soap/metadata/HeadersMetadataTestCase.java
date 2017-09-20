/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.metadata;

import static java.lang.Thread.currentThread;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsIn.isIn;
import static org.junit.Assert.assertThat;
import static org.mule.test.allure.AllureConstants.WscFeature.WSC_EXTENSION;

import org.mule.metadata.api.annotation.TypeIdAnnotation;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.api.model.NullType;
import org.mule.metadata.api.model.ObjectFieldType;
import org.mule.metadata.api.model.ObjectType;
import org.mule.metadata.api.model.StringType;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.runtime.soap.api.client.SoapClientConfiguration;
import org.mule.runtime.soap.api.client.metadata.SoapOperationMetadata;
import org.mule.service.soap.client.TestSoapClient;

import java.net.URL;
import java.util.Collection;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.hamcrest.core.Is;
import org.junit.Test;

@Feature(WSC_EXTENSION)
@Story("Metadata")
public class HeadersMetadataTestCase extends AbstractMetadataTestCase {

  @Test
  @Description("Checks the input Soap Headers metadata for an operation with headers")
  public void operationWithInputHeadersMetadata() throws MetadataResolvingException {
    SoapOperationMetadata result = resolver.getInputMetadata("echoWithHeaders");
    ObjectType headers = toObjectType(result.getHeadersType());
    Collection<ObjectFieldType> fields = headers.getFields();
    assertThat(fields, hasSize(2));
    fields.forEach(field -> {
      Collection<ObjectFieldType> headerFields = ((ObjectType) field.getValue()).getFields();
      assertThat(headerFields, hasSize(1));
      MetadataType headerField = headerFields.iterator().next().getValue();
      assertThat(headerField, is(instanceOf(StringType.class)));
    });
  }

  @Test
  @Description("Checks the input Soap Headers metadata for an operation without headers")
  public void operationWithoutInputHeadersMetadata() throws MetadataResolvingException {
    SoapOperationMetadata result = resolver.getInputMetadata("echo");
    assertThat(result.getHeadersType(), is(instanceOf(NullType.class)));
  }

  @Test
  @Description("Checks the metadata for a Header that is defined in another message that is not the main operation message")
  public void operationWithCommonHeaderDefinedInDifferentMessageMetadata() throws MetadataResolvingException {
    URL humanWsdl = currentThread().getContextClassLoader().getResource("wsdl/human.wsdl");
    SoapClientConfiguration configuration = SoapClientConfiguration.builder()
        .withDispatcher(dispatcher)
        .withAddress("address.com")
        .withVersion(soapVersion)
        .withWsdlLocation(humanWsdl.getPath())
        .withService("Human_ResourcesService")
        .withPort("Human_Resources").build();
    TestSoapClient humanWsdlClient = new TestSoapClient(configuration);
    SoapOperationMetadata result = humanWsdlClient.getMetadataResolver().getInputMetadata("Get_Employee");
    ObjectType headers = toObjectType(result.getHeadersType());
    assertThat(headers.getFields(), hasSize(1));
    ObjectFieldType header = headers.getFields().iterator().next();
    assertThat(header.getKey().getName().getLocalPart(), is("header"));
    String headerTypeId = header.getValue().getAnnotation(TypeIdAnnotation.class).get().getValue();
    assertThat(headerTypeId, is("{urn:com.workday/bsvc}Workday_Common_Header"));
  }

  @Test
  @Description("Checks the Output Attributes Metadata for an operation with output soap headers")
  public void operationWithOutputSoapHeadersMetadata() throws MetadataResolvingException {
    SoapOperationMetadata result = resolver.getOutputMetadata("echoWithHeaders");
    ObjectType outputHeaders = toObjectType(result.getHeadersType());
    assertThat(outputHeaders.getFields(), hasSize(2));
    outputHeaders.getFields().forEach(e -> {
      assertThat(e.getKey().getName().getLocalPart(), isIn(new String[] {"headerOut", "headerInOut"}));
      MetadataType value = toObjectType(e.getValue()).getFields().iterator().next().getValue();
      assertThat(value, is(instanceOf(StringType.class)));
    });
  }

  @Test
  @Description("Checks the Output Attributes Metadata for an operation without output soap headers")
  public void operationWithoutOutputSoapHeadersMetadata() throws MetadataResolvingException {
    SoapOperationMetadata result = resolver.getOutputMetadata("echo");
    MetadataType outputHeaders = result.getHeadersType();
    assertThat(outputHeaders, is(instanceOf(NullType.class)));
  }

  @Test
  @Description("Checks the metadata for an operation that is ONE WAY")
  public void oneWayOperationMetadata() throws MetadataResolvingException {
    SoapOperationMetadata input = resolver.getInputMetadata("oneWay");
    SoapOperationMetadata output = resolver.getOutputMetadata("oneWay");
    assertThat(input.getHeadersType(), Is.is(instanceOf(NullType.class)));
    assertThat(output.getHeadersType(), Is.is(instanceOf(NullType.class)));
  }
}
