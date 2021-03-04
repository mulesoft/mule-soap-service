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
 * Base contract for Security Stores that prepares additional properties for CXF in order to apply some kind of Web Service
 * Security.
 * <p>
 * See https://ws.apache.org/wss4j/config.html.
 *
 * @since 1.0
 */
public interface WssStoreConfigurationPropertiesBuilder {

  /**
   * Prefix for all WSS4J crypto properties
   */
  String WSS4J_PROP_PREFIX = "org.apache.wss4j.crypto";

  /**
   * Prefix for all merlin crypto specific properties
   */
  String MERLIN_PROP_PREFIX = WSS4J_PROP_PREFIX + ".merlin.";

  /**
   * WSS4J property name to specify a provider used to create Crypto instances.
   */
  String WS_CRYPTO_PROVIDER_KEY = WSS4J_PROP_PREFIX + ".provider";

  /**
   * @return a set of {@link Properties} to configure a {@link SecurityStrategyCxfAdapter}.
   */
  Properties getConfigurationProperties();
}
