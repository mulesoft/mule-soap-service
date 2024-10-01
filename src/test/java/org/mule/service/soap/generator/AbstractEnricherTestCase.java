/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.generator;

import org.mule.metadata.xml.api.XmlTypeLoader;
import org.mule.service.soap.AbstractSoapServiceTestCase;
import org.mule.wsdl.parser.WsdlParser;
import org.mule.wsdl.parser.model.WsdlModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public abstract class AbstractEnricherTestCase extends AbstractSoapServiceTestCase {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  protected WsdlModel model;

  @Before
  public void setup() {
    model = WsdlParser.Companion.parse(server.getDefaultAddress() + "?wsdl");
  }
}
