/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.xml.util;

import org.dom4j.io.DOMReader;
import org.w3c.dom.Document;

/**
 * General utility methods for working with XML.
 * 
 * @since 1.0, Copied from the removed XML module.
 */
public class XMLUtils {

  /**
   * Converts a DOM to an XML string.
   * 
   * @param dom the dome object to convert
   * @return A string representation of the document
   */
  public static String toXml(Document dom) {
    return new DOMReader().read(dom).asXML();
  }

}
