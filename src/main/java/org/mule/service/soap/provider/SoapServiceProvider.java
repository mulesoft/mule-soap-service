/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.provider;

import static java.util.Collections.singletonList;
import org.mule.runtime.api.service.ServiceDefinition;
import org.mule.runtime.api.service.ServiceProvider;
import org.mule.runtime.soap.api.SoapService;
import org.mule.service.soap.SoapServiceImplementation;

import java.util.List;

/**
 * {@link ServiceProvider} implementation for providing a mule {@link SoapService}.
 *
 * @since 1.0
 */
public class SoapServiceProvider implements ServiceProvider {

  @Override
  public List<ServiceDefinition> providedServices() {
    ServiceDefinition serviceDefinition = new ServiceDefinition(SoapService.class, new SoapServiceImplementation());
    return singletonList(serviceDefinition);
  }
}