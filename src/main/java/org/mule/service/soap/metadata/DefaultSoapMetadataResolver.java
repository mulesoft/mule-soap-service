/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.metadata;

import org.mule.metadata.api.TypeLoader;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.runtime.soap.api.client.metadata.SoapMetadataResolver;
import org.mule.runtime.soap.api.client.metadata.SoapOperationMetadata;
import org.mule.wsdl.parser.model.PortModel;
import org.mule.wsdl.parser.model.WsdlModel;
import org.mule.wsdl.parser.model.operation.OperationModel;

import java.util.Map;
import java.util.Set;

/**
 * Default immutable {@link SoapMetadataResolver} implementation.
 *
 * @since 1.0
 */
public class DefaultSoapMetadataResolver implements SoapMetadataResolver {

  private final HeadersMetadataResolver inputHeadersResolver;
  private final HeadersMetadataResolver outputHeadersResolver;
  private final BodyMetadataResolver inputBodyResolver;
  private final BodyMetadataResolver outputBodyResolver;
  private final AttachmentsMetadataResolver inputAttachmentsResolver;
  private final AttachmentsMetadataResolver outputAttachmentsResolver;
  private final ServiceOperationsResolver keysResolver;

  public DefaultSoapMetadataResolver(WsdlModel wsdl, PortModel port, TypeLoader loader) {
    inputHeadersResolver = new InputHeadersMetadataResolver(wsdl, port, loader);
    outputHeadersResolver = new OutputHeadersMetadataResolver(wsdl, port, loader);
    outputAttachmentsResolver = new OutputAttachmentsMetadataResolver(port, loader);
    inputAttachmentsResolver = new InputAttachmentsMetadataResolver(port, loader);
    inputBodyResolver = new InputBodyMetadataResolver(port, loader);
    outputBodyResolver = new OutputBodyMetadataResolver(port, loader);
    keysResolver = new ServiceOperationsResolver(port);
  }

  @Override
  public SoapOperationMetadata getInputMetadata(String operation) throws MetadataResolvingException {
    return new ImmutableSoapOperationMetadata(inputBodyResolver.getMetadata(operation),
                                              inputHeadersResolver.getMetadata(operation),
                                              inputAttachmentsResolver.getMetadata(operation));
  }

  @Override
  public SoapOperationMetadata getOutputMetadata(String operation) throws MetadataResolvingException {
    return new ImmutableSoapOperationMetadata(outputBodyResolver.getMetadata(operation),
                                              outputHeadersResolver.getMetadata(operation),
                                              outputAttachmentsResolver.getMetadata(operation));
  }

  @Override
  public Set<String> getAvailableOperations() {
    return keysResolver.getAvailableOperations();
  }
}
