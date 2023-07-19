/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.metadata;

import static java.lang.String.format;
import static org.mule.metadata.api.model.MetadataFormat.XML;
import static org.mule.runtime.api.metadata.resolving.FailureCode.INVALID_CONFIGURATION;
import static org.mule.runtime.api.metadata.resolving.FailureCode.INVALID_METADATA_KEY;
import static org.mule.runtime.api.metadata.resolving.FailureCode.UNKNOWN;
import static org.mule.wsdl.parser.model.operation.OperationType.ONE_WAY;

import org.mule.metadata.api.TypeLoader;
import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.wsdl.parser.model.PortModel;
import org.mule.wsdl.parser.model.operation.OperationModel;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.wsdl.Part;

/**
 * Base class for metadata resolvers that resolve dynamic metadata of XML node elements.
 *
 * @since 1.0
 */
abstract class NodeMetadataResolver {

  final PortModel port;
  final BaseTypeBuilder typeBuilder = BaseTypeBuilder.create(XML);
  final MetadataType nullType = typeBuilder.nullType().build();
  final TypeLoader loader;
  final Function<OperationModel, Optional<Part>> bodyPartRetriever;

  NodeMetadataResolver(PortModel port,
                       TypeLoader loader,
                       Function<OperationModel, Optional<Part>> bodyPartRetriever) {
    this.port = port;
    this.loader = loader;
    this.bodyPartRetriever = bodyPartRetriever;
  }

  /**
   * Resolves the metadata for an operation, Input or Output is fetched depending on the {@link Function} passed as parameter.
   *
   * @param operation the name of the operation that the types are going to be resolved.
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

  Part getBodyPart(String operation) throws MetadataResolvingException {
    return bodyPartRetriever.apply(port.getOperation(operation))
        .orElseThrow(() -> {
          String errorMsg = "No body type found for operation [" + operation + "]";
          return new MetadataResolvingException(errorMsg, INVALID_METADATA_KEY);
        });
  }

  boolean isOneWay(String operationName) {
    return ONE_WAY.equals(port.getOperation(operationName).getType());
  }
}
