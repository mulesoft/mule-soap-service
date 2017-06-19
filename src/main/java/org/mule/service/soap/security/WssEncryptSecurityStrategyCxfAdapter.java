/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.security;


import static org.apache.wss4j.common.ConfigurationConstants.ENCRYPT;
import static org.apache.wss4j.common.ConfigurationConstants.ENCRYPTION_USER;
import static org.apache.wss4j.common.ConfigurationConstants.ENC_PROP_REF_ID;

import org.mule.runtime.extension.api.soap.security.config.WssKeyStoreConfiguration;
import org.mule.service.soap.security.callback.WSPasswordCallbackHandler;
import org.mule.service.soap.security.config.WssKeyStoreConfigurationPropertiesBuilder;
import com.google.common.collect.ImmutableMap;
import java.util.Map;

/**
 * Verifies the signature of a SOAP response, using certificates of the trust-store in the provided TLS context.
 *
 * @since 1.0
 */
public class WssEncryptSecurityStrategyCxfAdapter implements SecurityStrategyCxfAdapter {

  private static final String WS_ENCRYPT_PROPERTIES_KEY = "encryptProperties";

  private WssKeyStoreConfigurationPropertiesBuilder keyStoreConfiguration;

  public WssEncryptSecurityStrategyCxfAdapter(WssKeyStoreConfiguration keyStoreConfiguration) {
    this.keyStoreConfiguration = new WssKeyStoreConfigurationPropertiesBuilder(keyStoreConfiguration);
  }

  @Override
  public SecurityStrategyType securityType() {
    return SecurityStrategyType.OUTGOING;
  }

  @Override
  public String securityAction() {
    return ENCRYPT;
  }

  @Override
  public java.util.Optional<WSPasswordCallbackHandler> buildPasswordCallbackHandler() {
    return java.util.Optional.empty();
  }

  @Override
  public Map<String, Object> buildSecurityProperties() {
    return ImmutableMap.<String, Object>builder().put(ENC_PROP_REF_ID, WS_ENCRYPT_PROPERTIES_KEY)
        .put(WS_ENCRYPT_PROPERTIES_KEY, keyStoreConfiguration.getConfigurationProperties())
        .put(ENCRYPTION_USER, keyStoreConfiguration.getAlias())
        .build();
  }
}
