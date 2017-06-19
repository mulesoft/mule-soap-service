/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.security.config;


import org.mule.service.soap.security.SecurityStrategyCxfAdapter;

import java.util.Properties;

/**
 * Base contract for Security Stores that prepares additional properties for CXF in order to apply some
 * kind of Web Service Security.
 *
 * @since 1.0
 */
public interface WssStoreConfigurationPropertiesBuilder {



  String WSS4J_PROP_PREFIX = "org.apache.wss4j.crypto";

  String MERLIN_PROP_PREFIX = WSS4J_PROP_PREFIX + ".merlin.";

  /**
   * Name of the property where the crypto provider is defined.
   */
  //  "org.apache.wss4j.crypto". "org.apache.ws.security.crypto".
  String WS_CRYPTO_PROVIDER_KEY = WSS4J_PROP_PREFIX + ".provider";

  /**
   * @return a set of {@link Properties} to configure a {@link SecurityStrategyCxfAdapter}.
   */
  Properties getConfigurationProperties();
}
