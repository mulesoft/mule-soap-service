/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.ds;

import org.mule.runtime.api.metadata.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

/**
 * {@link DataSource} wapper for an {@link InputStream}.
 */
public final class InputStreamDataSource implements DataSource {

  private final InputStream data;
  private final MediaType contentType;
  private final String name;

  public InputStreamDataSource(InputStream data, MediaType contentType, String name) {
    this.data = data;
    this.contentType = contentType;
    this.name = name;
  }

  @Override
  public String getContentType() {
    return contentType.toString();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return data;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    throw new IOException("Cannot write into an InputStreamDataSource");
  }

}
