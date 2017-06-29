/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.service.soap.introspection;

import org.mule.runtime.soap.api.transport.TransportResourceLocator;
import org.apache.cxf.wsdl11.CatalogWSDLLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import javax.wsdl.xml.WSDLLocator;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link WSDLLocator} implementation that enables the retrieval of WSDL document and associated files
 * that are protected using a {@link TransportResourceLocator} instance.
 * <p>
 * If the {@link TransportResourceLocator} cannot retrieve a document then a delegate {@link CatalogWSDLLocator}
 * will try to retrieve it.
 *
 * @since 1.0
 */
final class WsdlLocator implements WSDLLocator {

  private static final Logger LOGGER = LoggerFactory.getLogger(WsdlLocator.class);

  private final WSDLLocator delegateLocator;
  private final TransportResourceLocator resourceLocator;
  private final List<InputStream> streams = new ArrayList<>();
  private final String wsdlLocation;

  /**
   * Mutable field, gets updated each time a new import is found
   */
  private String latestImportUri;

  WsdlLocator(String wsdlLocation, TransportResourceLocator resourceLocator) {
    this.wsdlLocation = wsdlLocation;
    this.delegateLocator = new CatalogWSDLLocator(wsdlLocation);
    this.resourceLocator = resourceLocator;
  }

  /**
   * Returns an InputSource "pointed at" the base document.
   * <p>
   * If the wsdl location can be fetched by the {@link TransportResourceLocator} it is consumed by it otherwise we
   * delegate the search to the delegate cxf {@link CatalogWSDLLocator}.
   */
  @Override
  public InputSource getBaseInputSource() {
    return resourceLocator.handles(wsdlLocation) ? getInputSource(wsdlLocation) : delegateLocator.getBaseInputSource();
  }

  /**
   * Returns an InputSource "pointed at" an imported wsdl document.
   * <p>
   * If the imported resource can be fetched by the {@link TransportResourceLocator} then it gets fetched, otherwise
   * the fetching is delegated to the {@link CatalogWSDLLocator}.
   */
  @Override
  public InputSource getImportInputSource(String parentLocation, String importLocation) {
    try {
      if (resourceLocator.handles(importLocation)) {
        latestImportUri = importLocation;
        return getInputSource(latestImportUri);
      } else {
        InputSource importInputSource = delegateLocator.getImportInputSource(parentLocation, importLocation);
        latestImportUri = delegateLocator.getLatestImportURI();
        return importInputSource;
      }
    } catch (Exception e) {
      throw new RuntimeException("Error retrieving the following WSDL resource: " + latestImportUri, e);
    }
  }

  /**
   * @return an URI representing the location of the base document.
   */
  @Override
  public String getBaseURI() {
    return wsdlLocation;
  }

  /**
   * @return an URI representing the location of the last import document
   * to be resolved. This is used in resolving nested imports where an
   * import location is relative to the parent document.
   */
  @Override
  public String getLatestImportURI() {
    return latestImportUri;
  }

  /**
   * Releases all the {@link InputStream}s opened to parse the wsdl and resource files.
   */
  @Override
  public void close() {
    streams.forEach(stream -> {
      try {
        stream.close();
      } catch (IOException e) {
        LOGGER.error("Error closing resource stream during WSDL retrieval", e);
      }
    });
  }

  private InputSource getInputSource(String url) {
    try {
      InputStream resultStream = resourceLocator.getResource(url);
      streams.add(resultStream);
      return new InputSource(resultStream);
    } catch (Exception e) {
      throw new IllegalArgumentException("Error fetching the resource [" + url + "]: " + e.getMessage(), e);
    }
  }
}
