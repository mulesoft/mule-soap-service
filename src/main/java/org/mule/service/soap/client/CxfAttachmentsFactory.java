/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.client;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static org.mule.runtime.core.api.util.IOUtils.toDataHandler;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_DISPOSITION;

import com.google.common.collect.ImmutableMap;
import org.apache.cxf.attachment.AttachmentImpl;
import org.apache.cxf.message.Attachment;
import org.mule.runtime.extension.api.soap.SoapAttachment;
import org.mule.runtime.soap.api.exception.BadRequestException;

import java.io.IOException;
import java.util.Map;

/**
 * A factory for CXF {@link Attachment}s.
 * <p>
 * Creates the CXF {@link Attachment}s from {@link SoapAttachment}
 *
 * @since 1.2
 */
class CxfAttachmentsFactory {

  private boolean isMtom;

  CxfAttachmentsFactory(boolean isMtom) {
    this.isMtom = isMtom;
  }

  public Map<String, Attachment> transformToCxfAttachments(Map<String, SoapAttachment> attachments) {
    if (!isMtom) {
      return emptyMap();
    }
    ImmutableMap.Builder<String, Attachment> builder = ImmutableMap.builder();
    attachments.forEach((name, value) -> {
      try {
        AttachmentImpl attachment = new AttachmentImpl(name, toDataHandler(name, value.getContent(), value.getContentType()));
        attachment.setHeader(CONTENT_DISPOSITION, "attachment; name=\"" + name + "\"");
        builder.put(name, attachment);
      } catch (IOException e) {
        throw new BadRequestException(format("Error while preparing attachment [%s] for upload", name), e);
      }
    });
    return builder.build();
  }

}
