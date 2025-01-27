/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.unit;

import org.junit.Test;
import org.mule.service.soap.util.XmlTransformationUtils;

import javax.xml.stream.XMLStreamReader;

import static javax.xml.stream.XMLStreamConstants.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class XmlTransformationUtilsTestCase {

  @Test
  public void stringToXmlStreamReaderPreservesCDataElements() throws Exception {

    String cdataContent = "<xml>M4cr1<xml/>!#EWDSA!@#!@#@!";
    String data = "<data><![CDATA[" + cdataContent + "]]></data>";
    XMLStreamReader reader = XmlTransformationUtils.stringToXmlStreamReader(data);

    boolean foundCdata = false;
    int event = reader.next();
    while (reader.hasNext()) {
      switch (event) {
        case CDATA:
          foundCdata = true;
          break;
      }
      event = reader.next();
    }

    assertThat(foundCdata, is(true));
  }

  @Test
  public void stringToXmlStreamReader() throws Exception {
    String data = "<data><data2>Juani</data2></data>";
    XMLStreamReader reader = XmlTransformationUtils.stringToXmlStreamReader(data);

    int startTagCount = 0;
    int endTagCount = 0;
    String textFound = "";

    int event = reader.next();
    while (reader.hasNext()) {
      switch (event) {
        case END_ELEMENT:
          endTagCount++;
          break;
        case CHARACTERS:
          textFound = reader.getText();
          break;
        case START_ELEMENT:
          startTagCount++;
          break;
        default:
          break;
      }
      event = reader.next();
    }

    assertThat(startTagCount, is(2));
    assertThat(textFound, is("Juani"));
    assertThat(endTagCount, is(2));
  }


}
