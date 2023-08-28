/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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
