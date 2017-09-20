/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.message;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static org.mule.runtime.api.metadata.MediaType.BINARY;

import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;
import org.mule.runtime.extension.api.soap.SoapAttachment;
import org.mule.runtime.extension.api.soap.SoapAttributes;
import org.mule.runtime.extension.api.soap.SoapOutputPayload;
import org.mule.runtime.soap.api.message.SoapResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * Empty {@link SoapResponse} implementation.
 *
 * @since 1.0
 */
public final class EmptySoapResponse implements SoapResponse {

  private final Map<String, String> transportHeaders;

  public EmptySoapResponse(Map<String, String> transportHeaders) {
    this.transportHeaders = unmodifiableMap(transportHeaders);
  }

  @Override
  public InputStream getContent() {
    return new ByteArrayInputStream(new byte[0]);
  }

  @Override
  public Map<String, String> getSoapHeaders() {
    return emptyMap();
  }

  @Override
  public Map<String, String> getTransportHeaders() {
    return transportHeaders;
  }

  @Override
  public Map<String, SoapAttachment> getAttachments() {
    return emptyMap();
  }

  @Override
  public MediaType getContentType() {
    return null;
  }

  @Override
  public Result<SoapOutputPayload, SoapAttributes> getAsResult(StreamingHelper helper) {
    return Result.<SoapOutputPayload, SoapAttributes>builder()
        .output(new SoapOutputPayload(new TypedValue<>(getContent(), DataType.builder()
            .type(InputStream.class)
            .mediaType(BINARY).build()), emptyMap(), emptyMap()))
        .attributes(new SoapAttributes(transportHeaders))
        .build();
  }
}
