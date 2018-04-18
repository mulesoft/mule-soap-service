/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.service.soap.unit;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.service.soap.client.SoapCxfClient.MULE_ATTACHMENTS_KEY;
import static org.mule.service.soap.client.SoapCxfClient.MULE_HEADERS_KEY;
import static org.mule.service.soap.client.SoapCxfClient.MULE_TRANSPORT_HEADERS_KEY;

import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.soap.api.message.SoapResponse;
import org.mule.service.soap.generator.SoapResponseGenerator;
import org.mule.service.soap.generator.attachment.AttachmentResponseEnricher;
import org.mule.service.soap.xml.util.XMLUtils;

import com.google.common.collect.ImmutableMap;

import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import javax.swing.text.Document;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.message.Exchange;
import org.junit.Test;

public class SoapResponseGeneratorTestCase {

  private static final String TEST_CHARSET = "windows-1252";

  @Test
  public void responseWithCharset() throws XMLStreamException {
    AttachmentResponseEnricher enricher = mock(AttachmentResponseEnricher.class);
    when(enricher.enrich(any(), any(), any())).thenAnswer(a -> XMLUtils.toXml(a.getArgumentAt(0, org.w3c.dom.Document.class)));
    Exchange exchange = mock(Exchange.class);
    when(exchange.get(MULE_ATTACHMENTS_KEY)).thenReturn(emptyMap());
    when(exchange.get(MULE_HEADERS_KEY)).thenReturn(emptyMap());
    when(exchange.get(MULE_TRANSPORT_HEADERS_KEY)).thenReturn(singletonMap("content-type", "text/html; charset=" + TEST_CHARSET));

    SoapResponseGenerator generator = new SoapResponseGenerator(enricher);
    SoapResponse response = generator.generate("dummy", new Object[] {getTestXml()}, exchange);
    MediaType contentType = response.getContentType();

    assertThat(contentType.getCharset().get().toString(), is(TEST_CHARSET));
  }

  private XMLStreamReader getTestXml() throws XMLStreamException {
    String text = "<foo>This is some XML</foo>";
    Reader reader = new StringReader(text);
    XMLInputFactory factory = XMLInputFactory.newInstance(); // Or newFactory()
    return factory.createXMLStreamReader(reader);
  }
}
