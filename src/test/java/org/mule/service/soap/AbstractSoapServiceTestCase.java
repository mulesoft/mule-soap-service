/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mule.runtime.soap.api.SoapVersion.SOAP11;
import static org.mule.runtime.soap.api.SoapVersion.SOAP12;
import static org.mule.service.soap.client.TestSoapClient.getDefaultConfiguration;

import org.mule.runtime.extension.api.soap.security.SecurityStrategy;
import org.mule.runtime.soap.api.SoapVersion;
import org.mule.service.soap.client.DefaultTestDispatcher;
import org.mule.service.soap.client.TestSoapClient;
import org.mule.service.soap.server.HttpServer;
import org.mule.service.soap.service.Soap11Service;
import org.mule.service.soap.service.Soap12Service;
import org.mule.tck.junit4.rule.DynamicPort;

import java.util.Collection;
import java.util.List;

import org.apache.cxf.interceptor.Interceptor;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public abstract class AbstractSoapServiceTestCase {

  public final SoapTestXmlValues testValues = new SoapTestXmlValues("http://service.soap.service.mule.org/");

  public final DefaultTestDispatcher dispatcher = new DefaultTestDispatcher();

  @Rule
  public DynamicPort port = new DynamicPort("port");

  @Parameterized.Parameter
  public SoapVersion soapVersion;

  @Parameterized.Parameter(1)
  public String serviceClass;

  protected TestSoapClient client;
  protected HttpServer server;

  @Parameterized.Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    return asList(new Object[][] {
        {SOAP11, Soap11Service.class.getName()},
        {SOAP12, Soap12Service.class.getName()}
    });
  }

  @Before
  public void before() throws Exception {
    XMLUnit.setIgnoreWhitespace(true);
    server = new HttpServer(port.getNumber(), buildInInterceptor(), buildOutInterceptor(), createServiceInstance());;
    client = new TestSoapClient(getDefaultConfiguration(server.getDefaultAddress())
        .enableMtom(isMtom())
        .withVersion(soapVersion)
        .withDispatcher(dispatcher)
        .withSecurities(getSecurityStrategies())
        .build());
  }

  protected boolean isMtom() {
    return false;
  }

  @After
  public void tearDown() throws Exception {
    client.stop();
    dispatcher.stop();
  }

  protected List<SecurityStrategy> getSecurityStrategies() {
    return emptyList();
  }

  protected String getServiceClass() {
    return serviceClass;
  }

  protected Interceptor buildInInterceptor() {
    return null;
  }

  protected Interceptor buildOutInterceptor() {
    return null;
  }

  private Object createServiceInstance() throws Exception {
    Class<?> serviceClass = this.getClass().getClassLoader().loadClass(getServiceClass());
    return serviceClass.newInstance();
  }
}
