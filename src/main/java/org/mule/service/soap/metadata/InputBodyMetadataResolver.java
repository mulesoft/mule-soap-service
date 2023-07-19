/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.metadata;

import org.mule.metadata.api.TypeLoader;
import org.mule.wsdl.parser.model.PortModel;
import org.mule.wsdl.parser.model.operation.OperationModel;

import java.util.Map;

/**
 * {@link BodyMetadataResolver} implementation for the output soap body.
 *
 * @since 1.0
 */
public final class InputBodyMetadataResolver extends BodyMetadataResolver {

  InputBodyMetadataResolver(PortModel operations, TypeLoader loader) {
    super(operations, loader, OperationModel::getInputBodyPart);
  }
}
