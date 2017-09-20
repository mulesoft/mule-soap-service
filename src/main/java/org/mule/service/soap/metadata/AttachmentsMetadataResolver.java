/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.metadata;

import static org.mule.metadata.api.utils.MetadataTypeUtils.getLocalPart;
import static org.mule.service.soap.util.SoapServiceMetadataTypeUtils.getAttachmentFields;

import org.mule.metadata.api.TypeLoader;
import org.mule.metadata.api.builder.ObjectTypeBuilder;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.api.model.ObjectFieldType;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.service.soap.introspection.OperationDefinition;
import org.mule.service.soap.introspection.ServiceDefinition;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.wsdl.Part;

/**
 * Handles the dynamic {@link MetadataType} resolution for the SOAP attachments of a web service operation.
 *
 * @since 1.0
 */
abstract class AttachmentsMetadataResolver extends NodeMetadataResolver {

  AttachmentsMetadataResolver(ServiceDefinition definition,
                              TypeLoader loader,
                              Function<OperationDefinition, Optional<Part>> partRetriever) {
    super(definition, loader, partRetriever);
  }

  @Override
  public MetadataType getMetadata(String operation) throws MetadataResolvingException {
    Part bodyPart = getBodyPart(definition.getOperation(operation));
    MetadataType bodyType = buildPartMetadataType(bodyPart);
    List<ObjectFieldType> attachments = getAttachmentFields(bodyType);
    if (attachments.isEmpty()) {
      return nullType;
    }
    ObjectTypeBuilder type = typeBuilder.objectType();
    attachments.forEach(attachment -> type.addField()
        .key(getLocalPart(attachment))
        .value(attachment.getValue()));
    return type.build();
  }
}
