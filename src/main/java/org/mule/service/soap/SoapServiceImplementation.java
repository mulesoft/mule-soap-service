/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
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
