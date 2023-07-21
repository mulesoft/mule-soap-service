/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.provider;

import org.mule.runtime.api.service.ServiceDefinition;
import org.mule.runtime.api.service.ServiceProvider;
import org.mule.runtime.soap.api.SoapService;
import org.mule.service.soap.SoapServiceImplementation;

/**
 * {@link ServiceProvider} implementation for providing a mule {@link SoapService}.
 *
 * @since 1.0
 */
public class SoapServiceProvider implements ServiceProvider {

  @Override
  public ServiceDefinition getServiceDefinition() {
    return new ServiceDefinition(SoapService.class, new SoapServiceImplementation());
  }
}
