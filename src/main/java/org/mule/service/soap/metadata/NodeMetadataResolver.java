/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.metadata;

import static java.lang.String.format;
import static javax.wsdl.OperationType.ONE_WAY;
import static org.mule.metadata.api.model.MetadataFormat.XML;
import static org.mule.runtime.api.metadata.resolving.FailureCode.INVALID_CONFIGURATION;
import static org.mule.runtime.api.metadata.resolving.FailureCode.INVALID_METADATA_KEY;
import static org.mule.runtime.api.metadata.resolving.FailureCode.UNKNOWN;

import org.mule.metadata.api.TypeLoader;
import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.service.soap.introspection.OperationDefinition;
import org.mule.service.soap.introspection.ServiceDefinition;

import java.util.Optional;
import java.util.function.Function;

import javax.wsdl.Part;

/**
 * Base class for metadata resolvers that resolve dynamic metadata of XML node elements.
 *
 * @since 1.0
 */
abstract class NodeMetadataResolver {

  final BaseTypeBuilder typeBuilder = BaseTypeBuilder.create(XML);
  final MetadataType nullType = typeBuilder.nullType().build();
  final ServiceDefinition definition;
  final TypeLoader loader;
  final Function<OperationDefinition, Optional<Part>> bodyPartRetriever;

  NodeMetadataResolver(ServiceDefinition definition,
                       TypeLoader loader,
                       Function<OperationDefinition, Optional<Part>> bodyPartRetriever) {
    this.definition = definition;
    this.loader = loader;
    this.bodyPartRetriever = bodyPartRetriever;
  }

  /**
   * Resolves the metadata for an operation, Input or Output is fetched depending on the {@link Function} passed
   * as parameter.
   *
   * @param operation   the name of the operation that the types are going to be resolved.
   * @throws MetadataResolvingException in any error case.
   */
  abstract MetadataType getMetadata(String operation) throws MetadataResolvingException;

  MetadataType buildPartMetadataType(Part part) throws MetadataResolvingException {
    if (part.getElementName() != null) {
      String partName = part.getElementName().toString();
      return loader.load(partName)
          .orElseThrow(() -> new MetadataResolvingException(format("Could not load part element name [%s]", partName), UNKNOWN));
    }
    throw new MetadataResolvingException("Trying to resolve metadata for a nameless part, probably the provided WSDL is invalid",
                                         INVALID_CONFIGURATION);
  }

  Part getBodyPart(OperationDefinition operation) throws MetadataResolvingException {
    return bodyPartRetriever.apply(operation)
        .orElseThrow(() -> {
          String errorMsg = "No body type found for operation [" + operation.getName() + "]";
          return new MetadataResolvingException(errorMsg, INVALID_METADATA_KEY);
        });
  }

  boolean isOneWay(String operationName) {
    return ONE_WAY.equals(definition.getOperation(operationName).getType());
  }
}
