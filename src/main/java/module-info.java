/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
import org.mule.api.annotation.jpms.ServiceModule;

/**
 * A SOAP service based on CXF.
 *
 * @moduleGraph
 * @since 1.5
 */
@ServiceModule
module org.mule.service.soap {

  requires org.mule.runtime.metadata.model.api;
  requires org.mule.runtime.api;
  requires org.mule.runtime.extensions.api;
  requires org.mule.runtime.extensions.soap.api;
  requires org.mule.runtime.http.api;
  requires org.mule.runtime.soap.api;
  // lifecycle api
  requires org.mule.runtime.core;

  requires org.apache.cxf.binding.soap;
  requires org.apache.cxf.core;
  requires org.apache.cxf.frontend.simple;
  requires org.apache.cxf.ws.security;
  requires org.apache.cxf.wsdl;
  requires org.apache.wss4j.common;
  requires org.apache.wss4j.dom;

  requires org.mule.wsdl.parser;
  requires mule.wsdl4j;

  requires com.ctc.wstx;
  requires dom4j;
  requires stax.utils;
  requires Saxon.HE;

  requires com.google.common;
  requires org.apache.commons.io;
  requires org.apache.commons.lang3;

  requires jakarta.activation;
  requires java.logging;
  requires java.xml.soap;

  // Allow invocation and injection into providers by the Mule Runtime
  exports org.mule.service.soap.provider to
      org.mule.runtime.service;

}
