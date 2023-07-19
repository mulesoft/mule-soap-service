/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.metadata;

import static java.lang.Thread.currentThread;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.mule.test.allure.AllureConstants.WscFeature.WSC_EXTENSION;

import org.mule.metadata.api.model.*;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.runtime.soap.api.client.SoapClientConfiguration;
import org.mule.runtime.soap.api.client.metadata.SoapMetadataResolver;
import org.mule.runtime.soap.api.client.metadata.SoapOperationMetadata;

import java.net.URL;
import java.util.Collection;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.Assert;
import org.junit.Test;
import org.mule.service.soap.client.TestSoapClient;

@Feature(WSC_EXTENSION)
@Story("Metadata")
public class AttachmentMetadataTestCase extends AbstractMetadataTestCase {

  @Test
  @Description("Checks the Input Metadata of an operation with required input attachments")
  public void operationWithInputAttachmentMetadata() throws MetadataResolvingException {
    SoapOperationMetadata result = resolver.getInputMetadata("uploadAttachment");
    ObjectType attachments = toObjectType(result.getAttachmentsType());
    Collection<ObjectFieldType> attachmentFields = attachments.getFields();
    assertThat(attachmentFields, hasSize(1));
    assertThat(attachmentFields.iterator().next().getKey().getName().getLocalPart(), is("attachment"));
  }

  @Test
  @Description("Checks the Input Metadata of an operation without attachments")
  public void operationWithoutInputAttachmentsMetadata() throws MetadataResolvingException {
    SoapOperationMetadata result = resolver.getInputMetadata("echo");
    assertThat(result.getAttachmentsType(), is(instanceOf(NullType.class)));
  }

  @Test
  @Description("Checks the Output Metadata of an operation that contains output attachments")
  public void operationWithOutputAttachmentsMetadata() throws MetadataResolvingException {
    SoapOperationMetadata result = resolver.getOutputMetadata("downloadAttachment");
    ObjectType objectType = toObjectType(result.getAttachmentsType());
    assertThat(objectType.getFields(), hasSize(1));
    ObjectFieldType attachment = objectType.getFields().iterator().next();
    assertThat(attachment.getKey().getName().getLocalPart(), is("attachment"));
    assertThat(attachment.getValue(), is(instanceOf(BinaryType.class)));
    assertThat(result.getBodyType(), is(instanceOf(NullType.class)));
  }

  @Test
  @Description("Checks the metadata for a multipart related output with a body and attachment")
  public void multipartOutputOperation() throws MetadataResolvingException {
    URL wsdl = currentThread().getContextClassLoader().getResource("wsdl/multipart-output/Multipart.wsdl");
    SoapClientConfiguration configuration = SoapClientConfiguration.builder()
        .withDispatcher(dispatcher)
        .withAddress("address.com")
        .withVersion(soapVersion)
        .withWsdlLocation(wsdl.getPath())
        .withService("MultipartService")
        .withPort("MultipartPort").build();
    TestSoapClient client = new TestSoapClient(configuration);
    SoapMetadataResolver resolver = client.getMetadataResolver();
    SoapOperationMetadata result = resolver.getOutputMetadata("retrieveDocument");
    MetadataType attachmentsType = result.getAttachmentsType();
    assertThat(toObjectType(attachmentsType).getFields().size(), is(1));
    MetadataType bodyType = result.getBodyType();
    Collection<ObjectFieldType> bodyFields = toObjectType(bodyType).getFields();
    assertThat(bodyFields.size(), is(1));
    assertThat(bodyFields.iterator().next().getValue().toString(), containsString("AnonType_RetrieveDocumentResponse"));
  }

  @Test
  @Description("Checks the metadata for an operation that is ONE WAY")
  public void oneWayOperationMetadata() throws MetadataResolvingException {
    SoapOperationMetadata input = resolver.getInputMetadata("oneWay");
    SoapOperationMetadata output = resolver.getOutputMetadata("oneWay");
    Assert.assertThat(input.getAttachmentsType(), is(instanceOf(NullType.class)));
    Assert.assertThat(output.getAttachmentsType(), is(instanceOf(NullType.class)));
  }
}
