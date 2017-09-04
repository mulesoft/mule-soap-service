/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.introspection;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mule.service.soap.SoapTestUtils.assertSimilarXml;

import org.mule.runtime.core.api.util.IOUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class WsdlSchemasCollectorTestCase {

  private static final String RECURSIVE_WSDL_FOLDER = "wsdl/recursive/";

  @Test
  public void wsdlWithEmbeddedTypeSchema() throws Exception {
    ClassLoader cl = currentThread().getContextClassLoader();
    URL wsdl = cl.getResource("wsdl/simple-service.wsdl");
    WsdlDefinition definition = new WsdlDefinition(wsdl.getPath(), "TestService", "TestPort");
    Map<String, InputStream> schemas = definition.getSchemas().collect();
    assertThat(schemas.size(), is(1));

    String expected = IOUtils.toString(cl.getResource("schemas/simple-service-types.xsd").openStream());
    String result = IOUtils.toString(schemas.entrySet().iterator().next().getValue());
    assertSimilarXml(expected, result);
  }

  /**
   * This test collects a set of local schemas referenced from a WSDL, this schemas also reference each other recursively.
   */
  @Test
  public void wsdlWithLocalRecursiveSchemas() throws Exception {
    String embeddedSchema = "schemas/recursive-embedded-schema.xsd";
    String wsdl = getResourceLocation(RECURSIVE_WSDL_FOLDER + "main.wsdl");
    WsdlDefinition definition = new WsdlDefinition(wsdl, "RecursiveService", "RecursivePort");
    Map<String, InputStream> schemas = definition.getSchemas().collect();
    List<String> files = asList(RECURSIVE_WSDL_FOLDER + "dir1/import0.xsd",
                                RECURSIVE_WSDL_FOLDER + "dir1/dir2/import1.xsd",
                                RECURSIVE_WSDL_FOLDER + "import2.xsd",
                                RECURSIVE_WSDL_FOLDER + "import3.xsd",
                                RECURSIVE_WSDL_FOLDER + "import4.xsd",
                                embeddedSchema);
    assertThat(schemas.values(), hasSize(files.size()));
    for (String file : files) {
      // If the file is the embedded schema the key is path to the wsdl containing the schema, thats why we check this
      String key = file.equals(embeddedSchema) ? wsdl : schemas.keySet().stream().filter(k -> k.contains(file)).findAny().get();
      assertSimilarXml(IOUtils.toString(schemas.get(key)), new FileInputStream(getResourceLocation(file)));
    }
  }

  @Test
  public void wsdlWithSchemaThatDoesNotHaveALocation() throws Exception {
    String wsdl = getResourceLocation("wsdl/no-schema-location/test.wsdl");
    WsdlDefinition definition = new WsdlDefinition(wsdl, "service", "BasicHttpBinding_IOrderService");
    Map<String, InputStream> schemas = definition.getSchemas().collect();
    assertThat(schemas.entrySet(), hasSize(4));
  }

  private String getResourceLocation(String name) {
    return Thread.currentThread().getContextClassLoader().getResource(name).getFile();
  }
}
