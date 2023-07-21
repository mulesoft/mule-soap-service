/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.unit;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
import org.mule.runtime.core.internal.streaming.bytes.ByteArrayCursorStream;
import org.mule.runtime.extension.api.soap.SoapOutputPayload;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
  public void toStringFullPayload() {
    TypedValue<InputStream> body = TypedValue.of(new ByteArrayInputStream("<xml>ABC</xml>".getBytes(UTF_8)));
    Map<String, TypedValue<String>> hs = ImmutableMap.of("header1", TypedValue.of("<header1>content</header1>"),
                                                         "header2", TypedValue.of("<header2>content</header2>"));
    Map<String, TypedValue<InputStream>> as = ImmutableMap.of("attachment1", TypedValue.of(null),
                                                              "attachment2", TypedValue.of(null));
    String result = new SoapOutputPayload(body, as, hs).toString();
    assertThat(result, is("{\n"
        + "body:<xml>ABC</xml>,\n"
        + "headers: [\"<header1>content</header1>\",\n"
        + "  \"<header2>content</header2>\"],\n"
        + "attachments: [attachment1, attachment2]\n"
        + "}"));
  }

  @Test
  public void withCursorProvier() {
    CursorStreamProvider mock = mock(CursorStreamProvider.class);
    when(mock.openCursor()).thenReturn(new ByteArrayCursorStream(mock, "<xml>ABC</xml>".getBytes(UTF_8)));
    TypedValue body = TypedValue.of(mock);
    Map<String, TypedValue<String>> hs = ImmutableMap.of("header1", TypedValue.of("<header1>content</header1>"),
                                                         "header2", TypedValue.of("<header2>content</header2>"));
    Map<String, TypedValue<InputStream>> as = ImmutableMap.of("attachment1", TypedValue.of(null),
                                                              "attachment2", TypedValue.of(null));
    String result = new SoapOutputPayload(body, as, hs).toString();
    assertThat(result, is("{\n"
        + "body:<xml>ABC</xml>,\n"
        + "headers: [\"<header1>content</header1>\",\n"
        + "  \"<header2>content</header2>\"],\n"
        + "attachments: [attachment1, attachment2]\n"
        + "}"));
  }

}
