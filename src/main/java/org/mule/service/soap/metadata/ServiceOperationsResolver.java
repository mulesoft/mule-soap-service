/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.metadata;

import static java.util.stream.Collectors.toSet;

import org.mule.metadata.api.model.MetadataType;
import org.mule.service.soap.introspection.OperationDefinition;
import org.mule.service.soap.introspection.ServiceDefinition;

import java.util.Set;

/**
 * Handles the dynamic {@link MetadataType} resolution for the SOAP Body parts of a web service operation.
 *
 * @since 1.0
 */
final class ServiceOperationsResolver {

  private final ServiceDefinition definition;

  ServiceOperationsResolver(ServiceDefinition definition) {
    this.definition = definition;
  }

  Set<String> getAvailableOperations() {
    return definition.getOperations().stream().map(OperationDefinition::getName).collect(toSet());
  }
}
