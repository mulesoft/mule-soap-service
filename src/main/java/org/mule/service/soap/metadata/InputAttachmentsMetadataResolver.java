/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.metadata;

import org.mule.metadata.api.TypeLoader;
import org.mule.wsdl.parser.model.PortModel;
import org.mule.wsdl.parser.model.operation.OperationModel;

import java.util.Map;

import static java.util.Optional.*;

/**
 * {@link AttachmentsMetadataResolver} implementation for the input attachments.
 *
 * @since 1.0
 */
public final class InputAttachmentsMetadataResolver extends AttachmentsMetadataResolver {

  InputAttachmentsMetadataResolver(PortModel port, TypeLoader loader) {
    super(port, loader, OperationModel::getInputBodyPart, o -> of(o.getInputMessage()));
  }
}
