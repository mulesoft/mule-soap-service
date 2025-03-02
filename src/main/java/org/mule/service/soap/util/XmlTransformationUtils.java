/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.util;

import static javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD;
import static javax.xml.XMLConstants.ACCESS_EXTERNAL_STYLESHEET;
import static javax.xml.stream.XMLInputFactory.IS_COALESCING;

import org.mule.runtime.core.api.util.xmlsecurity.XMLSecureFactories;
import org.mule.runtime.soap.api.SoapService;
import org.mule.service.soap.xml.stax.StaxSource;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.ctc.wstx.stax.WstxInputFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import net.sf.saxon.jaxp.SaxonTransformerFactory;

/**
 * {@link SoapService} Transformation utility class
 *
 * @since 1.0
 */
public class XmlTransformationUtils {

  private static final XMLInputFactory XML_INPUT_FACTORY = getXmlInputFactory();
  private static final TransformerFactory SAXON_TRANSFORMER_FACTORY;

  static {
    final TransformerFactory factory = new SaxonTransformerFactory();
    factory.setAttribute(ACCESS_EXTERNAL_STYLESHEET, "");
    factory.setAttribute(ACCESS_EXTERNAL_DTD, "");
    SAXON_TRANSFORMER_FACTORY = factory;
  }

  private static XMLInputFactory getXmlInputFactory() {
    XMLInputFactory xmlInputFactory = new WstxInputFactory();
    // Preserve the CDATA tags
    xmlInputFactory.setProperty(IS_COALESCING, false);
    return xmlInputFactory;
  }

  public static Document xmlStreamReaderToDocument(XMLStreamReader xmlStreamReader) throws XmlTransformationException {
    StaxSource staxSource = new StaxSource(xmlStreamReader);
    DOMResult writer = new DOMResult();
    try {
      Transformer transformer = SAXON_TRANSFORMER_FACTORY.newTransformer();
      transformer.transform(staxSource, writer);
    } catch (TransformerException e) {
      throw new XmlTransformationException("Error transforming reader to DOM document", e);
    }
    return (Document) writer.getNode();
  }

  public static Element stringToDomElement(String xml) throws XmlTransformationException {
    try {
      DocumentBuilder db = XMLSecureFactories.createDefault().getDocumentBuilderFactory().newDocumentBuilder();
      InputSource is = new InputSource();
      is.setCharacterStream(new StringReader(xml));
      return db.parse(is).getDocumentElement();
    } catch (Exception e) {
      throw new XmlTransformationException("Could not transform xml string to Dom Element", e);
    }
  }

  public static Document stringToDocument(String xml) throws XmlTransformationException {
    DocumentBuilderFactory factory = XMLSecureFactories.createDefault().getDocumentBuilderFactory();
    try {
      factory.setNamespaceAware(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      return builder.parse(new ByteArrayInputStream(xml.getBytes()));
    } catch (Exception e) {
      throw new XmlTransformationException("Could not transform xml to Dom Document", e);
    }
  }

  public static String nodeToString(Node node) throws XmlTransformationException {
    try {
      StringWriter writer = new StringWriter();
      DOMSource source = new DOMSource(node);
      StreamResult result = new StreamResult(writer);
      TransformerFactory idTransformer = SaxonTransformerFactory.newInstance();
      Transformer transformer = idTransformer.newTransformer();
      transformer.transform(source, result);
      return writer.toString();
    } catch (Exception e) {
      throw new XmlTransformationException("Could not transform Node to String", e);
    }
  }

  public static XMLStreamReader stringToXmlStreamReader(String xml) throws XmlTransformationException {
    return stringToXmlStreamReader(xml, "UTF-8");
  }

  public static XMLStreamReader stringToXmlStreamReader(String xml, String encoding) throws XmlTransformationException {
    try {
      return XML_INPUT_FACTORY.createXMLStreamReader(new ByteArrayInputStream(xml.getBytes(Charset.forName(encoding))));
    } catch (Exception e) {
      throw new XmlTransformationException("Could not transform xml to XmlStreamReader", e);
    }
  }
}
