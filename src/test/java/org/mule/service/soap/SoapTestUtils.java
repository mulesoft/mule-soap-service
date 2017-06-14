/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap;

import static org.custommonkey.xmlunit.XMLUnit.compareXML;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.custommonkey.xmlunit.XMLUnit;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.extension.api.soap.SoapAttachment;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ru.yandex.qatools.allure.annotations.Step;

public class SoapTestUtils {

  public static final String XML = ".xml";

  // Operations
  public static final String ECHO = "echo";
  public static final String NO_PARAMS = "noParams";
  public static final String FAIL = "fail";
  public static final String UPLOAD_ATTACHMENT = "uploadAttachment";
  public static final String DOWNLOAD_ATTACHMENT = "downloadAttachment";

  public static String getResponseResource(final String responseResourceName) throws IOException, XMLStreamException {
    return resourceAsString("response/" + responseResourceName + XML);
  }

  public static String getRequestResource(final String requestResourceName) throws IOException, XMLStreamException {
    return resourceAsString("request/" + requestResourceName + XML);
  }

  public static String resourceAsString(final String resource) throws XMLStreamException, IOException {
    final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
    StringWriter writer = new StringWriter();
    IOUtils.copy(is, writer);
    return writer.toString();
  }

  public static void assertSimilarXml(String expected, String result) throws Exception {
    XMLUnit.setIgnoreWhitespace(true);
    Diff diff = compareXML(result, expected);
    if (!diff.similar()) {
      System.out.println("Expected xml is:\n");
      System.out.println(prettyPrint(expected));
      System.out.println("########################################\n");
      System.out.println("But got:\n");
      System.out.println(prettyPrint(result));
    }
    assertThat(diff.similar(), is(true));
  }

  private static String prettyPrint(String a)
      throws TransformerException, ParserConfigurationException, IOException, SAXException {
    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    InputSource is = new InputSource();
    is.setCharacterStream(new StringReader(a));
    Document doc = db.parse(is);
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    // initialize StreamResult with File object to save to file
    StreamResult result = new StreamResult(new StringWriter());
    DOMSource source = new DOMSource(doc);
    transformer.transform(source, result);
    return result.getWriter().toString();
  }

  @Step("Prepares a test attachment")
  public static SoapAttachment getTestAttachment() {
    SoapAttachment attachment = mock(SoapAttachment.class);
    when(attachment.getContent()).thenReturn(IOUtils.toInputStream("Some Content"));
    when(attachment.getContentType()).thenReturn(MediaType.TEXT);
    return attachment;
  }
}