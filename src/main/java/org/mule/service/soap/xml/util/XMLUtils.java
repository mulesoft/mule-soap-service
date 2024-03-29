/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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
