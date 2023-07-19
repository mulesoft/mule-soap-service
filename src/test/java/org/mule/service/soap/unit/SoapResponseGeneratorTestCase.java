/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.unit;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
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

import java.io.Reader;
import java.io.StringReader;

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
    when(enricher.enrich(any(), any(), any())).thenAnswer(a -> XMLUtils.toXml(a.getArgument(0)));
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
