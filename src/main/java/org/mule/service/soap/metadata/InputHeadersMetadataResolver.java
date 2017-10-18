/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.service.soap.metadata;

import org.mule.metadata.api.TypeLoader;
import org.mule.wsdl.parser.model.PortModel;
import org.mule.wsdl.parser.model.WsdlModel;
import org.mule.wsdl.parser.model.operation.OperationModel;

import java.util.Map;

/**
 * {@link HeadersMetadataResolver} implementation for input headers metadata.
 *
 * @since 1.0
 */
public class InputHeadersMetadataResolver extends HeadersMetadataResolver {

  public InputHeadersMetadataResolver(WsdlModel wsdl, PortModel port, TypeLoader loader) {
    super(wsdl, port, loader, OperationModel::getInputMessage, OperationModel::getInputHeaders);
  }
}
