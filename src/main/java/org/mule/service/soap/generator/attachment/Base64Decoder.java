/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.generator.attachment;

import static org.mule.runtime.api.metadata.DataType.BYTE_ARRAY;
import static org.mule.runtime.api.metadata.DataType.CURSOR_STREAM_PROVIDER;
import static org.mule.runtime.api.metadata.DataType.INPUT_STREAM;
import static org.mule.runtime.api.metadata.DataType.STRING;
import static org.mule.runtime.core.api.config.i18n.CoreMessages.transformFailed;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
import org.mule.runtime.core.api.transformer.AbstractTransformer;
import org.mule.runtime.core.api.transformer.TransformerException;
import org.mule.runtime.core.api.util.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * <code>Base64Encoder</code> transforms Base64 encoded data into strings or byte arrays.
 *
 * @since 1.0
 */
public class Base64Decoder extends AbstractTransformer {

  public Base64Decoder() {
    registerSourceType(STRING);
    registerSourceType(BYTE_ARRAY);
    registerSourceType(INPUT_STREAM);
    registerSourceType(CURSOR_STREAM_PROVIDER);
    setReturnDataType(BYTE_ARRAY);
  }

  @Override
  public Object doTransform(Object src, Charset outputEncoding) throws TransformerException {
    try {
      String data;

      if (src instanceof byte[]) {
        data = new String((byte[]) src, outputEncoding);
      } else if (src instanceof CursorStreamProvider) {
        data = handleStream(((CursorStreamProvider) src).openCursor(), outputEncoding);
      } else if (src instanceof InputStream) {
        data = handleStream((InputStream) src, outputEncoding);
      } else {
        data = (String) src;
      }

      byte[] result = Base64.decode(data);

      if (STRING.isCompatibleWith(getReturnDataType())) {
        return new String(result, outputEncoding);
      } else {
        return result;
      }
    } catch (Exception ex) {
      throw new TransformerException(transformFailed("base64", getReturnDataType()), this, ex);
    }
  }

  private String handleStream(InputStream input, Charset outputEncoding) throws IOException {
    String data;
    try {
      data = org.apache.commons.io.IOUtils.toString(input, outputEncoding);
    } finally {
      input.close();
    }
    return data;
  }

}
