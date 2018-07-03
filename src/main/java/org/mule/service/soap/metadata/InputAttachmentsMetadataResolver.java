/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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
