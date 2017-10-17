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
import org.mule.wsdl.parser.model.WsdlModel;
import org.mule.wsdl.parser.model.operation.OperationModel;
import org.mule.wsdl.parser.model.operation.SoapHeader;

import java.util.List;
import java.util.Map;
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

  private final WsdlModel wsdl;
  private final Function<OperationModel, Message> messageRetriever;
  private final Function<OperationModel, List<SoapHeader>> headersRetriever;

  HeadersMetadataResolver(WsdlModel wsdl,
                          Map<String, OperationModel> operations,
                          TypeLoader loader,
                          Function<OperationModel, Message> messageRetriever,
                          Function<OperationModel, List<SoapHeader>> headersRetriever) {
    super(operations, loader, o -> Optional.empty());
    this.wsdl = wsdl;
    this.messageRetriever = messageRetriever;
    this.headersRetriever = headersRetriever;
  }

  @Override
  public MetadataType getMetadata(String operationName) throws MetadataResolvingException {
    OperationModel operation = operations.get(operationName);
    List<SoapHeader> headers = headersRetriever.apply(operation);
    if (!headers.isEmpty()) {
      return buildHeaderType(headers, messageRetriever.apply(operation));
    }
    return nullType;
  }

  private MetadataType buildHeaderType(List<SoapHeader> headers, Message message)
      throws MetadataResolvingException {
    ObjectTypeBuilder objectType = typeBuilder.objectType();
    for (SoapHeader header : headers) {
      ObjectFieldTypeBuilder field = objectType.addField();
      String headerPart = header.getPartName();
      Part part = message.getPart(headerPart);
      if (part != null) {
        field.key(headerPart).value(buildPartMetadataType(part));
      } else {
        Message headerMessage = wsdl.getMessage(header.getQName());
        field.key(headerPart).value(buildPartMetadataType(headerMessage.getPart(headerPart)));
      }
    }
    return objectType.build();
  }

}
