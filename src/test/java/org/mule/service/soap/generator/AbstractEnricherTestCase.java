/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
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
