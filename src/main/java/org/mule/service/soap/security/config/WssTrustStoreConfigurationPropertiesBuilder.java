/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.security.config;

import static org.apache.wss4j.common.crypto.Merlin.TRUSTSTORE_FILE;
import static org.apache.wss4j.common.crypto.Merlin.TRUSTSTORE_PASSWORD;
import static org.apache.wss4j.common.crypto.Merlin.TRUSTSTORE_TYPE;

import org.mule.runtime.extension.api.soap.security.config.WssTrustStoreConfiguration;
import org.apache.wss4j.common.crypto.Merlin;
import java.util.Properties;


/**
 * Default {@link WssStoreConfigurationPropertiesBuilder} implementation for Trust Stores, used for signature verification.
 *
 * @since 1.0
 */
public class WssTrustStoreConfigurationPropertiesBuilder implements WssStoreConfigurationPropertiesBuilder {

  private String trustStorePath;
  private String password;
  private String type;

  public WssTrustStoreConfigurationPropertiesBuilder(WssTrustStoreConfiguration trustStoreConfiguration) {
    this.password = trustStoreConfiguration.getPassword();
    this.trustStorePath = trustStoreConfiguration.getStorePath();
    this.type = trustStoreConfiguration.getType();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Properties getConfigurationProperties() {
    Properties properties = new Properties();
    properties.setProperty(WS_CRYPTO_PROVIDER_KEY, Merlin.class.getCanonicalName());
    properties.setProperty(MERLIN_PROP_PREFIX + TRUSTSTORE_FILE, trustStorePath);
    properties.setProperty(MERLIN_PROP_PREFIX + TRUSTSTORE_TYPE, type);
    properties.setProperty(MERLIN_PROP_PREFIX + TRUSTSTORE_PASSWORD, password);
    return properties;
  }
}
