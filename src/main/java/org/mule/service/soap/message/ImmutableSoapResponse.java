/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.message;

import static java.util.Collections.unmodifiableMap;
import static org.mule.runtime.api.metadata.MediaType.XML;

import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;
import org.mule.runtime.extension.api.soap.SoapAttachment;
import org.mule.runtime.extension.api.soap.SoapAttributes;
import org.mule.runtime.extension.api.soap.SoapOutputPayload;
import org.mule.runtime.soap.api.message.SoapResponse;
import com.google.common.collect.ImmutableMap;
import java.io.InputStream;
import java.util.Map;

/**
 * Immutable {@link SoapResponse} implementation.
 *
 * @since 1.0
 */
public final class ImmutableSoapResponse implements SoapResponse {

  private final InputStream content;
  private final Map<String, String> soapHeaders;
  private final Map<String, String> transportHeaders;
  private final Map<String, SoapAttachment> attachments;
  private final MediaType contentType;

  public ImmutableSoapResponse(InputStream content,
                               Map<String, String> soapHeaders,
                               Map<String, String> transportHeaders,
                               Map<String, SoapAttachment> attachments,
                               MediaType contentType) {
    this.content = content;
    this.soapHeaders = unmodifiableMap(soapHeaders);
    this.transportHeaders = unmodifiableMap(transportHeaders);
    this.attachments = unmodifiableMap(attachments);
    this.contentType = contentType;
  }

  @Override
  public InputStream getContent() {
    return content;
  }

  @Override
  public Map<String, String> getSoapHeaders() {
    return soapHeaders;
  }

  @Override
  public Map<String, String> getTransportHeaders() {
    return transportHeaders;
  }

  @Override
  public Map<String, SoapAttachment> getAttachments() {
    return attachments;
  }

  @Override
  public MediaType getContentType() {
    return contentType;
  }

  @Override
  public Result<SoapOutputPayload, SoapAttributes> getAsResult(StreamingHelper helper) {
    return Result.<SoapOutputPayload, SoapAttributes>builder()
        .output(new SoapOutputPayload(wrapBody(content, helper), wrapAttachments(attachments, helper), soapHeaders))
        .attributes(new SoapAttributes(transportHeaders))
        .build();
  }

  private TypedValue<InputStream> wrapBody(InputStream body, StreamingHelper helper) {
    return new TypedValue(helper.resolveCursorProvider(body), DataType.builder().type(InputStream.class).mediaType(XML).build());
  }

  private Map<String, TypedValue<InputStream>> wrapAttachments(Map<String, SoapAttachment> attachments, StreamingHelper helper) {
    ImmutableMap.Builder<String, TypedValue<InputStream>> wrapped = ImmutableMap.builder();
    attachments.forEach((k, v) -> wrapped.put(k, new TypedValue(helper.resolveCursorProvider(v.getContent()),
                                                                DataType.builder().type(InputStream.class)
                                                                    .mediaType(v.getContentType()).build())));
    return wrapped.build();
  }

}
