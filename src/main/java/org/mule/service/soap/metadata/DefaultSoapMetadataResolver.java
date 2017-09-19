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
import org.mule.service.soap.introspection.ServiceDefinition;

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

  public DefaultSoapMetadataResolver(ServiceDefinition definition, TypeLoader loader) {
    inputHeadersResolver = new InputHeadersMetadataResolver(definition, loader);
    outputHeadersResolver = new OutputHeadersMetadataResolver(definition, loader);
    outputAttachmentsResolver = new OutputAttachmentsMetadataResolver(definition, loader);
    inputAttachmentsResolver = new InputAttachmentsMetadataResolver(definition, loader);
    inputBodyResolver = new InputBodyMetadataResolver(definition, loader);
    outputBodyResolver = new OutputBodyMetadataResolver(definition, loader);
    keysResolver = new ServiceOperationsResolver(definition);
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
