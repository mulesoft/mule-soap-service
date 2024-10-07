/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.util;

import org.mule.runtime.api.metadata.MediaType;
import org.mule.service.soap.ds.ByteArrayDataSource;
import org.mule.service.soap.ds.InputStreamDataSource;
import org.mule.service.soap.ds.StringDataSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;

public class AttachmentsUtils {

  /**
   * Transforms an Object into a DataHandler of its corresponding type.
   *
   * @param name        the name of the attachment being handled
   * @param object      the attachment to be handled
   * @param contentType the Content-Type of the attachment that is being handled
   * @return a {@link DataHandler} of the corresponding attachment
   * @throws IOException if the transformation fails.
   */
  public static DataHandler toDataHandler(String name, Object object, MediaType contentType) throws IOException {
    DataHandler dh;
    if (object instanceof File) {
      if (contentType != null) {
        dh = new DataHandler(new FileInputStream((File) object), contentType.toString());
      } else {
        dh = new DataHandler(new FileDataSource((File) object));
      }
    } else if (object instanceof URL) {
      if (contentType != null) {
        dh = new DataHandler(((URL) object).openStream(), contentType.toString());
      } else {
        dh = new DataHandler((URL) object);
      }
    } else if (object instanceof String) {
      if (contentType != null) {
        dh = new DataHandler(new StringDataSource((String) object, name, contentType));
      } else {
        dh = new DataHandler(new StringDataSource((String) object, name));
      }
    } else if (object instanceof byte[] && contentType != null) {
      dh = new DataHandler(new ByteArrayDataSource((byte[]) object, contentType, name));
    } else if (object instanceof InputStream && contentType != null) {
      dh = new DataHandler(new InputStreamDataSource((InputStream) object, contentType, name));
    } else {
      dh = new DataHandler(object, contentType != null ? contentType.toString() : null);
    }
    return dh;
  }

}
