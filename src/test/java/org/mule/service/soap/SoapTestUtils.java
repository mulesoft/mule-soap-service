/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap;

import static org.custommonkey.xmlunit.XMLUnit.compareXML;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
import org.mule.runtime.extension.api.soap.SoapOutputPayload;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

public class SoapTestUtils {

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

  public static void assertSimilarXml(String expected, InputStream result) throws Exception {
    assertSimilarXml(expected, IOUtils.toString(result));
  }

  private static String prettyPrint(String a) throws Exception {
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

  public static String payloadBodyAsString(Message message) throws Exception {
    Object payload = message.getPayload().getValue();
    assertThat(payload, is(instanceOf(SoapOutputPayload.class)));
    TypedValue<InputStream> body = ((SoapOutputPayload) payload).getBody();
    InputStream val =
        body.getValue() instanceof CursorStreamProvider ? ((CursorStreamProvider) body.getValue()).openCursor() : body.getValue();
    return IOUtils.toString(val);
  }
}
