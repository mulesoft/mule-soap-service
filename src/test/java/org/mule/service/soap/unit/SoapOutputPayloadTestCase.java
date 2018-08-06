/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.unit;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.extension.api.soap.SoapOutputPayload;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class SoapOutputPayloadTestCase {

  @Test
  public void toStringOnlyBody() {
    TypedValue<InputStream> body = new TypedValue<>(new ByteArrayInputStream("<xml>ABC</xml>".getBytes(UTF_8)), null);
    String result = new SoapOutputPayload(body, emptyMap(), emptyMap()).toString();
    assertThat(result, is("{\n"
        + "body:<xml>ABC</xml>,\n"
        + "headers: [],\n"
        + "attachments: []\n"
        + "}"));
  }

  @Test
  public void toStringFullPayload() throws Exception {
    TypedValue<InputStream> body = new TypedValue<>(new ByteArrayInputStream("<xml>ABC</xml>".getBytes(UTF_8)), null);
    Map<String, TypedValue<String>> hs = ImmutableMap.of("header1", new TypedValue<>("<header1>content</header1>", null),
                                                         "header2", new TypedValue<>("<header2>content</header2>", null));
    Map<String, TypedValue<InputStream>> as = ImmutableMap.of("attachment1", new TypedValue<>(null, null),
                                                              "attachment2", new TypedValue<>(null, null));
    String result = new SoapOutputPayload(body, as, hs).toString();
    assertThat(result, is("{\n"
        + "body:<xml>ABC</xml>,\n"
        + "headers: [\"<header1>content</header1>\",\n"
        + "  \"<header2>content</header2>\"],\n"
        + "attachments: [attachment1, attachment2]\n"
        + "}"));
  }
}
