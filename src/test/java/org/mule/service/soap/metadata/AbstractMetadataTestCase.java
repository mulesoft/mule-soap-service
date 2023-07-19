/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.metadata;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.api.model.ObjectType;
import org.mule.runtime.soap.api.client.metadata.SoapMetadataResolver;
import org.mule.service.soap.AbstractSoapServiceTestCase;

import org.junit.Before;

public abstract class AbstractMetadataTestCase extends AbstractSoapServiceTestCase {

  SoapMetadataResolver resolver;

  @Before
  public void setup() {
    resolver = client.getMetadataResolver();
  }

  ObjectType toObjectType(MetadataType type) {
    assertThat(type, is(instanceOf(ObjectType.class)));
    return (ObjectType) type;
  }

  protected MetadataType getMessageBuilderFieldType(MetadataType messageResult, String name) {
    ObjectType objectType = toObjectType(messageResult);
    return objectType.getFields().stream()
        .filter(f -> f.getKey().getName().getLocalPart().equals(name)).findAny().get().getValue();
  }
}
