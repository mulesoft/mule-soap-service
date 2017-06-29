/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.security.config;

import static org.apache.wss4j.common.crypto.Merlin.KEYSTORE_ALIAS;
import static org.apache.wss4j.common.crypto.Merlin.KEYSTORE_FILE;
import static org.apache.wss4j.common.crypto.Merlin.KEYSTORE_PASSWORD;
import static org.apache.wss4j.common.crypto.Merlin.KEYSTORE_PRIVATE_PASSWORD;
import static org.apache.wss4j.common.crypto.Merlin.KEYSTORE_TYPE;

import org.mule.runtime.extension.api.soap.security.config.WssKeyStoreConfiguration;
import org.apache.wss4j.common.crypto.Merlin;
import java.util.Properties;

/**
 * Default {@link WssStoreConfigurationPropertiesBuilder} implementation for Key Stores, used for encryption, decryption and signing.
 *
 * @since 1.0
 */
public class WssKeyStoreConfigurationPropertiesBuilder implements WssStoreConfigurationPropertiesBuilder {

  private String alias;
  private String keyPassword;
  private String password;
  private String keyStorePath;
  private String type;

  public WssKeyStoreConfigurationPropertiesBuilder(WssKeyStoreConfiguration keyStoreConfiguration) {
    this.alias = keyStoreConfiguration.getAlias();
    this.keyPassword = keyStoreConfiguration.getKeyPassword();
    this.password = keyStoreConfiguration.getPassword();
    this.keyStorePath = keyStoreConfiguration.getStorePath();
    this.type = keyStoreConfiguration.getType();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Properties getConfigurationProperties() {
    Properties properties = new Properties();
    properties.setProperty(WS_CRYPTO_PROVIDER_KEY, Merlin.class.getCanonicalName());
    properties.setProperty(MERLIN_PROP_PREFIX + KEYSTORE_TYPE, type);
    properties.setProperty(MERLIN_PROP_PREFIX + KEYSTORE_PASSWORD, password);
    properties.setProperty(MERLIN_PROP_PREFIX + KEYSTORE_ALIAS, alias);
    properties.setProperty(MERLIN_PROP_PREFIX + KEYSTORE_FILE, keyStorePath);

    if (keyPassword != null) {
      properties.setProperty(KEYSTORE_PRIVATE_PASSWORD, keyPassword);
    }

    return properties;
  }

  public String getKeyPassword() {
    return keyPassword;
  }

  public String getAlias() {
    return alias;
  }

  public String getPassword() {
    return password;
  }
}
