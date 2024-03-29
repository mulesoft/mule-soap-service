/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.metadata;

import org.mule.metadata.api.model.MetadataType;
import org.mule.wsdl.parser.model.PortModel;

import java.util.Set;

/**
 * Handles the dynamic {@link MetadataType} resolution for the SOAP Body parts of a web service operation.
 *
 * @since 1.0
 */
final class ServiceOperationsResolver {

  private final PortModel port;

  public ServiceOperationsResolver(PortModel port) {
    this.port = port;
  }

  Set<String> getAvailableOperations() {
    return port.getOperationsMap().keySet();
  }
}
