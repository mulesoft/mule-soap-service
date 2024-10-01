/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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
