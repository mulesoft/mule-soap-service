/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.security;

import static java.util.Optional.of;
import static org.apache.wss4j.common.ConfigurationConstants.DEC_PROP_REF_ID;
import static org.apache.wss4j.common.ConfigurationConstants.ENCRYPT;
import static org.apache.wss4j.common.ext.WSPasswordCallback.DECRYPT;

import org.mule.runtime.extension.api.soap.security.config.WssKeyStoreConfiguration;
import org.mule.service.soap.security.callback.WSPasswordCallbackHandler;
import org.mule.service.soap.security.config.WssKeyStoreConfigurationPropertiesBuilder;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Optional;

/**
 * Decrypts an encrypted SOAP response, using the private key of the key-store in the provided TLS context.
 *
 * @since 1.0
 */
public class WssDecryptSecurityStrategyCxfAdapter implements SecurityStrategyCxfAdapter {

  private static final String WS_DECRYPT_PROPERTIES_KEY = "decryptProperties";

  /**
   * The keystore to use when decrypting the message.
   */
  private WssKeyStoreConfigurationPropertiesBuilder keyStoreConfiguration;

  public WssDecryptSecurityStrategyCxfAdapter(WssKeyStoreConfiguration keyStoreConfiguration) {
    this.keyStoreConfiguration = new WssKeyStoreConfigurationPropertiesBuilder(keyStoreConfiguration);
  }

  @Override
  public SecurityStrategyType securityType() {
    return SecurityStrategyType.INCOMING;
  }

  @Override
  public String securityAction() {
    return ENCRYPT;
  }

  @Override
  public Optional<WSPasswordCallbackHandler> buildPasswordCallbackHandler() {
    return of(new WSPasswordCallbackHandler(DECRYPT, cb -> cb.setPassword(keyStoreConfiguration.getKeyPassword())));
  }

  @Override
  public Map<String, Object> buildSecurityProperties() {
    return ImmutableMap.<String, Object>builder()
        .put(DEC_PROP_REF_ID, WS_DECRYPT_PROPERTIES_KEY)
        .put(WS_DECRYPT_PROPERTIES_KEY, keyStoreConfiguration.getConfigurationProperties())
        .build();
  }
}
