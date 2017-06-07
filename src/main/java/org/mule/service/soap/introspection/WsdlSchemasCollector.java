/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.introspection;

import static com.google.common.collect.Maps.newHashMap;
import static java.lang.String.format;
import static org.mule.service.soap.util.XmlTransformationUtils.*;

import org.mule.metadata.xml.SchemaCollector;
import org.mule.runtime.soap.api.exception.InvalidWsdlException;
import org.mule.service.soap.util.XmlTransformationException;
import org.mule.service.soap.util.XmlTransformationUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Types;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.extensions.schema.SchemaReference;

/**
 * The purpose of this class is to find all the schema URLs, both local or remote, for a given WSDL definition. This includes
 * imports and includes in the WSDL file and recursively in each schema found.
 *
 * @since 1.0
 */
@SuppressWarnings("unchecked")
final class WsdlSchemasCollector {

  private final Map<String, Schema> schemas = newHashMap();
  private final Definition definition;

  WsdlSchemasCollector(Definition definition) {
    this.definition = definition;
  }

  public SchemaCollector collect() {
    SchemaCollector collector = SchemaCollector.getInstance();
    collectSchemas(definition);
    schemas.forEach((uri, schema) -> {
      try {
        collector.addSchema(uri, nodeToString(schema.getElement()));
      } catch (XmlTransformationException e) {
        String message = uri.endsWith(".wsdl") ? "Schema embedded in wsdl [%s]" : "Schema [%s]";
        throw new InvalidWsdlException(format(message + " could not be parsed", uri), e);
      }
    });
    return collector;
  }

  private void collectSchemas(Definition definition) {
    collectFromTypes(definition.getTypes());
    definition.getImports().values().forEach(wsdlImport -> {
      if (wsdlImport instanceof Import) {
        collectSchemas(((Import) wsdlImport).getDefinition());
      }
    });
  }

  private void collectFromTypes(Types types) {
    if (types != null) {
      types.getExtensibilityElements().forEach(element -> {
        if (element instanceof Schema) {
          Schema schema = (Schema) element;
          addSchema(schema);
        }
      });
    }
  }

  private void addSchema(Schema schema) {
    String key = schema.getDocumentBaseURI();
    if (!schemas.containsKey(key)) {
      schemas.put(key, schema);
      addImportedSchemas(schema);
      addIncludedSchemas(schema);
    }
  }

  private void addImportedSchemas(Schema schema) {
    Collection imports = schema.getImports().values();
    imports.forEach(vector -> ((Vector) vector).forEach(element -> {
      if (element instanceof SchemaImport) {
        Schema importedSchema = ((SchemaImport) element).getReferencedSchema();
        addSchema(importedSchema);
      }
    }));
  }

  private void addIncludedSchemas(Schema schema) {
    schema.getIncludes().forEach(include -> {
      if (include instanceof SchemaReference) {
        addSchema(((SchemaReference) include).getReferencedSchema());
      }
    });
  }
}
