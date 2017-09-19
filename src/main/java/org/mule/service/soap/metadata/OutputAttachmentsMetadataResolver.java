/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.service.soap.metadata;

import org.mule.metadata.api.TypeLoader;
import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.service.soap.introspection.OperationDefinition;
import org.mule.service.soap.introspection.ServiceDefinition;

/**
 * {@link AttachmentsMetadataResolver} implementation for the output attachments.
 *
 * @since 1.0
 */
final class OutputAttachmentsMetadataResolver extends AttachmentsMetadataResolver {

  OutputAttachmentsMetadataResolver(ServiceDefinition definition, TypeLoader loader) {
    super(definition, loader, OperationDefinition::getOutputBodyPart);
  }

  @Override
  public MetadataType getMetadata(String operation) throws MetadataResolvingException {
    return isOneWay(operation) ? nullType : super.getMetadata(operation);
  }
}
