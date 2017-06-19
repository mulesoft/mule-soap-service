/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.security;

import static java.util.Optional.of;
import static org.apache.wss4j.common.ConfigurationConstants.SIGNATURE_USER;
import static org.apache.wss4j.common.ConfigurationConstants.SIG_PROP_REF_ID;
import static org.apache.wss4j.common.ext.WSPasswordCallback.SIGNATURE;

import org.mule.runtime.extension.api.soap.security.config.WssKeyStoreConfiguration;
import org.mule.service.soap.security.callback.WSPasswordCallbackHandler;
import org.mule.service.soap.security.config.WssKeyStoreConfigurationPropertiesBuilder;
import com.google.common.collect.ImmutableMap;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import java.util.Map;
import java.util.Optional;


/**
 * Signs the SOAP request that is being sent, using the private key of the key-store in the provided TLS context.
 *
 * @since 1.0
 */
public class WssSignSecurityStrategyCxfAdapter implements SecurityStrategyCxfAdapter {

  private static final String WS_SIGN_PROPERTIES_KEY = "signProperties";

  /**
   * The keystore to use when signing the message.
   */
  private final WssKeyStoreConfigurationPropertiesBuilder keyStoreConfiguration;

  public WssSignSecurityStrategyCxfAdapter(WssKeyStoreConfiguration keyStoreConfiguration) {
    this.keyStoreConfiguration = new WssKeyStoreConfigurationPropertiesBuilder(keyStoreConfiguration);
  }

  @Override
  public SecurityStrategyType securityType() {
    return SecurityStrategyType.OUTGOING;
  }

  @Override
  public Optional<WSPasswordCallbackHandler> buildPasswordCallbackHandler() {
    return of(new WSPasswordCallbackHandler(SIGNATURE, cb -> cb.setPassword(keyStoreConfiguration.getPassword())));
  }

  @Override
  public String securityAction() {
    return WSHandlerConstants.SIGNATURE;
  }

  @Override
  public Map<String, Object> buildSecurityProperties() {
    return ImmutableMap.<String, Object>builder()
        .put(SIG_PROP_REF_ID, WS_SIGN_PROPERTIES_KEY)
        .put(WS_SIGN_PROPERTIES_KEY, keyStoreConfiguration.getConfigurationProperties())
        .put(SIGNATURE_USER, keyStoreConfiguration.getAlias())
        .build();
  }
}
