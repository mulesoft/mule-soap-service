/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.generator;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mule.runtime.api.metadata.DataType.STRING;
import static org.mule.runtime.core.api.util.Base64.encodeBytes;
import org.mule.runtime.core.api.transformer.Transformer;
import org.mule.service.soap.generator.attachment.Base64Decoder;
import org.mule.service.soap.generator.attachment.Base64Encoder;
import org.mule.tck.core.transformer.AbstractTransformerTestCase;

import org.junit.Test;

public class Base64TransformersTestCase extends AbstractTransformerTestCase {

  private static final String TEST_DATA = "the quick brown fox jumped over the lazy dog";

  @Override
  public Object getResultData() {
    try {
      return encodeBytes(TEST_DATA.getBytes());
    } catch (Exception ex) {
      fail();
      return null;
    }
  }

  @Override
  public Object getTestData() {
    return TEST_DATA;
  }

  @Override
  public Transformer getTransformer() {
    return new Base64Encoder();
  }

  @Override
  public Transformer getRoundTripTransformer() {
    Transformer t = new Base64Decoder();
    // our input is a String so we expect a String as output
    t.setReturnDataType(STRING);
    return t;
  }

  @Test
  public void decodeUnpaddedString() throws Exception {
    String encodeBytes = (String) getResultData();
    assertThat(encodeBytes, endsWith("="));
    while (encodeBytes.endsWith("=")) {
      encodeBytes = encodeBytes.substring(0, encodeBytes.length() - 1);
    }
    assertThat(encodeBytes, not(endsWith("=")));

    String resultString = (String) getRoundTripTransformer().transform(encodeBytes);

    assertThat(resultString, is(TEST_DATA));
  }
}
