/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.metadata;

import static java.lang.Thread.currentThread;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mule.test.allure.AllureConstants.WscFeature.WSC_EXTENSION;

import org.hamcrest.Matchers;
import org.mule.metadata.api.annotation.TypeIdAnnotation;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.api.model.NullType;
import org.mule.metadata.api.model.ObjectFieldType;
import org.mule.metadata.api.model.ObjectType;
import org.mule.metadata.api.model.StringType;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.runtime.soap.api.client.SoapClientConfiguration;
import org.mule.runtime.soap.api.client.metadata.SoapMetadataResolver;
import org.mule.runtime.soap.api.client.metadata.SoapOperationMetadata;

import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.Test;
import org.mule.service.soap.client.TestSoapClient;

@Feature(WSC_EXTENSION)
@Story("Metadata")
public class BodyMetadataTestCase extends AbstractMetadataTestCase {

  @Test
  @Description("Checks the dynamic metadata of an operation with required parameters")
  public void operationWithSimpleInputParameterMetadata() throws MetadataResolvingException {
    SoapOperationMetadata result = resolver.getInputMetadata("echo");
    MetadataType body = result.getBodyType();
    Collection<ObjectFieldType> fields = toObjectType(body).getFields();
    assertThat(fields, hasSize(1));
    ObjectFieldType operationField = fields.iterator().next();
    assertThat(operationField.getKey().getName().getLocalPart(), is("echo"));
    Collection<ObjectFieldType> operationParams = toObjectType(operationField.getValue()).getFields();
    assertThat(operationParams, hasSize(1));
    ObjectFieldType field = operationParams.iterator().next();
    assertThat(field.getKey().getName().getLocalPart(), is("text"));
    assertThat(field.getValue(), is(instanceOf(StringType.class)));
  }

  @Test
  @Description("Checks the dynamic metadata for an operation without input parameters")
  public void operationNoInputParametersMetadata() throws MetadataResolvingException {
    SoapOperationMetadata result = resolver.getInputMetadata("noParams");
    MetadataType body = result.getBodyType();
    Collection<ObjectFieldType> fields = toObjectType(body).getFields();
    assertThat(fields, hasSize(1));
    ObjectFieldType operationField = fields.iterator().next();
    assertThat(operationField.getKey().getName().getLocalPart(), is("noParams"));
    Collection<ObjectFieldType> operationParams = toObjectType(operationField.getValue()).getFields();
    assertThat(operationParams, hasSize(0));
  }

  @Test
  @Description("Checks the dynamic metadata of an operation with a complex input parameter")
  public void operationComplexInputParameterMetadata() throws MetadataResolvingException {
    SoapOperationMetadata result = resolver.getInputMetadata("echoAccount");
    MetadataType body = result.getBodyType();
    Collection<ObjectFieldType> fields = toObjectType(body).getFields();
    assertThat(fields, hasSize(1));
    ObjectFieldType operationField = fields.iterator().next();
    assertThat(operationField.getKey().getName().getLocalPart(), is("echoAccount"));

    Collection<ObjectFieldType> operationParams = toObjectType(operationField.getValue()).getFields();
    assertThat(operationParams, hasSize(2));
    Iterator<ObjectFieldType> iterator = operationParams.iterator();
    ObjectFieldType accountField = iterator.next();
    assertThat(accountField.getKey().getName().getLocalPart(), is("account"));

    ObjectType accountType = toObjectType(accountField.getValue());
    Collection<ObjectFieldType> accountFields = accountType.getFields();
    assertThat(accountFields, hasSize(4));

    ObjectFieldType name = iterator.next();
    assertThat(name.getKey().getName().getLocalPart(), is("name"));
    assertThat(name.getValue(), is(instanceOf(StringType.class)));
  }

  @Test
  @Description("Checks the Output Body Metadata for an operation that returns a simple string")
  public void operationWithSimpleOutputTypeMetadata() throws MetadataResolvingException {
    SoapOperationMetadata result = resolver.getOutputMetadata("echo");
    Collection<ObjectFieldType> resultFields = toObjectType(result.getBodyType()).getFields();
    assertThat(resultFields, hasSize(1));
    ObjectType echoType = toObjectType(resultFields.iterator().next().getValue());
    Collection<ObjectFieldType> echoFields = echoType.getFields();
    assertThat(echoFields, hasSize(1));
    ObjectFieldType textField = echoFields.iterator().next();
    assertThat(textField.getKey().getName().getLocalPart(), is("text"));
    assertThat(textField.getValue(), is(instanceOf(StringType.class)));
  }

  @Test
  @Description("Checks the Output Body Metadata for an operation that returns a complex element")
  public void operationWithComplexOutputTypeMetadata() throws MetadataResolvingException {
    SoapOperationMetadata result = resolver.getOutputMetadata("echoAccount");
    Collection<ObjectFieldType> resultFields = toObjectType(result.getBodyType()).getFields();
    assertThat(resultFields, hasSize(1));
    ObjectType echoType = toObjectType(resultFields.iterator().next().getValue());
    Collection<ObjectFieldType> echoFields = echoType.getFields();
    assertThat(echoFields, hasSize(1));
    ObjectFieldType accountField = echoFields.iterator().next();
    assertThat(accountField.getKey().getName().getLocalPart(), is("account"));
    ObjectType objectType = toObjectType(accountField.getValue());
    assertThat(objectType.getFields(), hasSize(4));
  }

  @Test
  @Description("Checks the metadata for an operation that is ONE WAY")
  public void oneWayOperationMetadata() throws MetadataResolvingException {
    SoapOperationMetadata input = resolver.getInputMetadata("oneWay");
    SoapOperationMetadata output = resolver.getOutputMetadata("oneWay");
    assertThat(output.getBodyType(), is(instanceOf(NullType.class)));
    assertThat(toObjectType(input.getBodyType()).getFields(), hasSize(1));
  }
}
