/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.metadata;

import org.mule.metadata.api.TypeLoader;
import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.wsdl.parser.model.PortModel;
import org.mule.wsdl.parser.model.operation.OperationModel;

import java.util.Map;

/**
 * {@link BodyMetadataResolver} implementation for the output soap body.
 *
 * @since 1.0
 */
final class OutputBodyMetadataResolver extends BodyMetadataResolver {

  OutputBodyMetadataResolver(PortModel definition, TypeLoader loader) {
    super(definition, loader, OperationModel::getOutputBodyPart);
  }

  @Override
  public MetadataType getMetadata(String operation) throws MetadataResolvingException {
    return isOneWay(operation) ? nullType : super.getMetadata(operation);
  }
}
