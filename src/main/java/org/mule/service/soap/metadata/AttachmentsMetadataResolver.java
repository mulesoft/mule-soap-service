/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.metadata;

import static java.lang.String.format;
import static org.mule.metadata.api.utils.MetadataTypeUtils.getLocalPart;
import static org.mule.runtime.api.metadata.resolving.FailureCode.INVALID_CONFIGURATION;
import static org.mule.service.soap.util.SoapServiceMetadataTypeUtils.getAttachmentFields;
import org.mule.metadata.api.TypeLoader;
import org.mule.metadata.api.builder.ObjectTypeBuilder;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.api.model.ObjectFieldType;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.service.soap.introspection.WsdlDefinition;

import java.util.List;

import javax.wsdl.Part;

/**
 * Handles the dynamic {@link MetadataType} resolution for the SOAP attachments of a web service operation.
 *
 * @since 1.0
 */
final class AttachmentsMetadataResolver extends NodeMetadataResolver {

  AttachmentsMetadataResolver(WsdlDefinition introspecter, TypeLoader loader) {
    super(introspecter, loader);
  }

  @Override
  public MetadataType getMetadata(String operation, TypeIntrospecterDelegate delegate) throws MetadataResolvingException {
    Part bodyPart = introspecter.getBodyPart(operation, delegate)
        .orElseThrow(() -> new MetadataResolvingException(format("operation [%s] does not have a body part", operation),
                                                          INVALID_CONFIGURATION));

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
