/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.client;

import static org.mule.runtime.core.api.util.ClassUtils.withContextClassLoader;

import org.apache.cxf.wsdl.service.factory.ReflectionServiceFactoryBean;
import org.mule.service.soap.conduit.SoapServiceConduitInitiator;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.binding.soap.SoapVersionFactory;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.databinding.stax.StaxDataBinding;
import org.apache.cxf.databinding.stax.StaxDataBindingFeature;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientFactoryBean;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.ConduitInitiatorManager;

import java.lang.reflect.Method;
import java.util.Iterator;

import javax.xml.transform.Source;

/**
 * A factory for CXF {@link Client}s.
 * <p>
 * Sets up the custom {@link SoapServiceConduitInitiator} for all the different entries used for CXF to obtain the needed
 * {@link Conduit}, this occurs because we want that CXF always use our custom conduit to operate.
 *
 * @since 1.0
 */
class CxfClientFactory {

  private final Bus bus;

  CxfClientFactory() {
    this.bus = withContextClassLoader(CxfClientFactory.class.getClassLoader(),
                                      () -> new SpringBusFactory().createBus((String) null, true));
    registerConduitInitiator(new SoapServiceConduitInitiator());
  }

  private void registerConduitInitiator(SoapServiceConduitInitiator initiator) {
    ConduitInitiatorManager extension = bus.getExtension(ConduitInitiatorManager.class);
    extension.registerConduitInitiator("http://cxf.apache.org/transports/http", initiator);
    extension.registerConduitInitiator("http://schemas.xmlsoap.org/wsdl/soap/", initiator);
    extension.registerConduitInitiator("http://schemas.xmlsoap.org/soap/http/", initiator);
    extension.registerConduitInitiator("http://schemas.xmlsoap.org/wsdl/soap/http", initiator);
    extension.registerConduitInitiator("http://schemas.xmlsoap.org/wsdl/http/", initiator);
    extension.registerConduitInitiator("http://www.w3.org/2003/05/soap/bindings/HTTP/", initiator);
    extension.registerConduitInitiator("http://cxf.apache.org/transports/http/configuration", initiator);
    extension.registerConduitInitiator("http://cxf.apache.org/bindings/xformat", initiator);
    extension.registerConduitInitiator("http://cxf.apache.org/transports/jms", initiator);
    extension.registerConduitInitiator("http://mule.codehaus.org/ws", initiator);
  }

  public Client createClient(String address, String soapVersion) {
    return withContextClassLoader(CxfClientFactory.class.getClassLoader(), () -> {
      ReflectionServiceFactoryBean serviceFactoryBean = new ReflectionServiceFactoryBean();
      serviceFactoryBean.getServiceConfigurations().add(0, new DefaultServiceConfiguration());
      ClientFactoryBean factory = new ClientFactoryBean(serviceFactoryBean);
      factory.setServiceClass(ProxyService.class);
      factory.setDataBinding(new StaxDataBinding());
      factory.getFeatures().add(new StaxDataBindingFeature());
      factory.setAddress(address);
      factory.setBus(bus);
      factory.setBindingId(getBindingIdForSoapVersion(soapVersion));
      return factory.create();
    });

  }

  private String getBindingIdForSoapVersion(String version) {
    Iterator<SoapVersion> soapVersions = SoapVersionFactory.getInstance().getVersions();
    while (soapVersions.hasNext()) {
      SoapVersion soapVersion = soapVersions.next();
      if (Double.toString(soapVersion.getVersion()).equals(version)) {
        return soapVersion.getBindingId();
      }
    }
    throw new IllegalArgumentException("Invalid Soap version " + version);
  }

  /**
   * Interface that describes the implementing the service.
   */
  private interface ProxyService {

    Source invoke(Source source);

    void invokeOneWay(Source source);

  }

  public class DefaultServiceConfiguration extends org.apache.cxf.wsdl.service.factory.DefaultServiceConfiguration {

    public Boolean hasOutMessage(Method m) {
      return !m.getName().equals("invokeOneWay");
    }

  }
}
