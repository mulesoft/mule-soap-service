/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap;

import org.mule.runtime.soap.api.SoapService;
import org.mule.runtime.soap.api.client.SoapClientFactory;
import org.mule.service.soap.client.SoapCxfClientFactory;

/**
 * Default Mule {@link SoapService} implementation.
 *
 * @since 1.0
 */
public class SoapServiceImplementation implements SoapService {

  @Override
  public String getName() {
    return "SOAP Service";
  }

  @Override
  public SoapClientFactory getClientFactory() {
    return new SoapCxfClientFactory();
  }
}
