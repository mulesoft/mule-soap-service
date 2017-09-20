/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.metadata;

import org.mule.metadata.api.TypeLoader;
import org.mule.metadata.api.builder.ObjectFieldTypeBuilder;
import org.mule.metadata.api.builder.ObjectTypeBuilder;
import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.service.soap.introspection.OperationDefinition;
import org.mule.service.soap.introspection.ServiceDefinition;
import org.mule.service.soap.introspection.SoapHeaderAdapter;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.wsdl.Message;
import javax.wsdl.Part;

/**
 * Handles the dynamic {@link MetadataType} resolution for the SOAP Headers of a web service operation.
 *
 * @since 1.0
 */
abstract class HeadersMetadataResolver extends NodeMetadataResolver {

  private final Function<OperationDefinition, Message> messageRetriever;
  private final Function<OperationDefinition, List<SoapHeaderAdapter>> headersRetriever;

  HeadersMetadataResolver(ServiceDefinition definition,
                          TypeLoader loader,
                          Function<OperationDefinition, Message> messageRetriever,
                          Function<OperationDefinition, List<SoapHeaderAdapter>> headersRetriever) {
    super(definition, loader, o -> Optional.empty());
    this.messageRetriever = messageRetriever;
    this.headersRetriever = headersRetriever;
  }

  @Override
  public MetadataType getMetadata(String operationName) throws MetadataResolvingException {
    OperationDefinition operation = definition.getOperation(operationName);
    List<SoapHeaderAdapter> headers = headersRetriever.apply(operation);
    if (!headers.isEmpty()) {
      return buildHeaderType(headers, messageRetriever.apply(operation));
    }
    return nullType;
  }

  private MetadataType buildHeaderType(List<SoapHeaderAdapter> headers, Message message)
      throws MetadataResolvingException {
    ObjectTypeBuilder objectType = typeBuilder.objectType();
    for (SoapHeaderAdapter header : headers) {
      ObjectFieldTypeBuilder field = objectType.addField();
      String headerPart = header.getPart();
      Part part = message.getPart(headerPart);
      if (part != null) {
        field.key(headerPart).value(buildPartMetadataType(part));
      } else {
        Message headerMessage = definition.getMessage(header.getMessage());
        field.key(headerPart).value(buildPartMetadataType(headerMessage.getPart(headerPart)));
      }
    }
    return objectType.build();
  }

}
