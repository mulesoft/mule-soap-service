/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.introspection;

import static java.lang.Thread.currentThread;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mule.service.soap.SoapTestUtils.assertSimilarXml;

import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.runtime.core.api.util.IOUtils;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class WsdlSchemasCollectorTestCase {

  private static final String RECURSIVE_WSDL_FOLDER = "wsdl/recursive/";

  @Test
  public void wsdlWithEmbeddedTypeSchema() throws Exception {
    ClassLoader cl = currentThread().getContextClassLoader();
    URL wsdl = cl.getResource("wsdl/simple-service.wsdl");
    ServiceDefinition definition = new ServiceDefinition(wsdl.getPath(), "TestService", "TestPort");
    Map<String, InputStream> schemas = definition.getSchemas().collect();
    assertThat(schemas.size(), is(1));
    String expected = IOUtils.toString(cl.getResource("schemas/simple-service-types.xsd").openStream());
    String result = IOUtils.toString(schemas.entrySet().iterator().next().getValue());
    assertSimilarXml(expected, result);
  }

  @Test
  public void wsdlWithLocalRecursiveSchemas() throws Exception {
    String wsdl = getResourceLocation(RECURSIVE_WSDL_FOLDER + "main.wsdl");
    ServiceDefinition definition = new ServiceDefinition(wsdl, "RecursiveService", "RecursivePort");
    Map<String, InputStream> schemas = definition.getSchemas().collect();
    assertThat(schemas.values(), hasSize(6));
  }

  @Test
  public void wsdlWithSchemaThatDoesNotHaveALocation() throws Exception {
    String wsdl = getResourceLocation("wsdl/no-schema-location/test.wsdl");
    ServiceDefinition definition = new ServiceDefinition(wsdl, "service", "BasicHttpBinding_IOrderService");
    Map<String, InputStream> schemas = definition.getSchemas().collect();
    assertThat(schemas.entrySet(), hasSize(4));
  }

  @Test
  public void multipleSchemasInTypesTag() throws MetadataResolvingException {
    String wsdl = getResourceLocation("wsdl/types-multiple-schema.wsdl");
    ServiceDefinition definition = new ServiceDefinition(wsdl, "TService", "TPort");
    Set<String> schemas = definition.getSchemas().collect().keySet();
    assertThat(schemas, hasSize(2));
    assertThat(schemas, hasItems("http://www.test.com/schemas/FirstInterface", "http://www.test.com/schemas/SecondInterface"));
  }

  private String getResourceLocation(String name) {
    return Thread.currentThread().getContextClassLoader().getResource(name).getFile();
  }
}
