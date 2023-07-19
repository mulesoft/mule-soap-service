/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.metadata;

import org.mule.metadata.api.TypeLoader;
import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.wsdl.parser.model.PortModel;
import org.mule.wsdl.parser.model.operation.OperationModel;
import org.mule.wsdl.parser.model.operation.OperationType;

import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mule.wsdl.parser.model.operation.OperationType.ONE_WAY;

/**
 * {@link AttachmentsMetadataResolver} implementation for the output attachments.
 *
 * @since 1.0
 */
final class OutputAttachmentsMetadataResolver extends AttachmentsMetadataResolver {

  OutputAttachmentsMetadataResolver(PortModel port, TypeLoader loader) {
    super(port, loader, OperationModel::getOutputBodyPart, o -> o.getType().equals(ONE_WAY) ? empty() : of(o.getOutputMessage()));
  }

  @Override
  public MetadataType getMetadata(String operation) throws MetadataResolvingException {
    return isOneWay(operation) ? nullType : super.getMetadata(operation);
  }
}
