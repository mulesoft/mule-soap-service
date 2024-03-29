/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.security;


import static org.apache.wss4j.common.ConfigurationConstants.SIGNATURE;
import static org.apache.wss4j.common.ConfigurationConstants.SIG_PROP_REF_ID;
import static org.apache.wss4j.common.crypto.Merlin.LOAD_CA_CERTS;
import static org.mule.service.soap.security.config.WssStoreConfigurationPropertiesBuilder.WS_CRYPTO_PROVIDER_KEY;

import org.mule.runtime.extension.api.soap.security.config.WssTrustStoreConfiguration;
import org.mule.service.soap.security.callback.WSPasswordCallbackHandler;
import org.mule.service.soap.security.config.WssStoreConfigurationPropertiesBuilder;
import org.mule.service.soap.security.config.WssTrustStoreConfigurationPropertiesBuilder;
import com.google.common.collect.ImmutableMap;
import org.apache.wss4j.common.crypto.Merlin;
import java.util.Map;
import java.util.Properties;


/**
 * Verifies the signature of a SOAP response, using certificates of the trust-store in the provided TLS context.
 *
 * @since 1.0
 */
public class WssVerifySignatureSecurityStrategyCxfAdapter implements SecurityStrategyCxfAdapter {

  private static final String WS_VERIFY_SIGNATURE_PROPERTIES_KEY = "verifySignatureProperties";

  /**
   * The truststore to use to verify the signature.
   */
  private final WssTrustStoreConfigurationPropertiesBuilder trustStoreConfiguration;

  public WssVerifySignatureSecurityStrategyCxfAdapter(WssTrustStoreConfiguration trustStoreConfiguration) {
    this.trustStoreConfiguration = new WssTrustStoreConfigurationPropertiesBuilder(trustStoreConfiguration);
  }

  public WssVerifySignatureSecurityStrategyCxfAdapter() {
    this.trustStoreConfiguration = null;
  }

  @Override
  public SecurityStrategyType securityType() {
    return SecurityStrategyType.INCOMING;
  }

  @Override
  public java.util.Optional<WSPasswordCallbackHandler> buildPasswordCallbackHandler() {
    return java.util.Optional.empty();
  }

  @Override
  public String securityAction() {
    return SIGNATURE;
  }

  @Override
  public Map<String, Object> buildSecurityProperties() {
    Properties signatureProps = trustStoreConfiguration != null ? trustStoreConfiguration.getConfigurationProperties()
        : getDefaultTrustStoreConfigurationProperties();

    return ImmutableMap.<String, Object>builder()
        .put(SIG_PROP_REF_ID, WS_VERIFY_SIGNATURE_PROPERTIES_KEY)
        .put(WS_VERIFY_SIGNATURE_PROPERTIES_KEY, signatureProps)
        .build();
  }

  private Properties getDefaultTrustStoreConfigurationProperties() {
    Properties properties = new Properties();
    properties.setProperty(WS_CRYPTO_PROVIDER_KEY, Merlin.class.getCanonicalName());
    properties.setProperty(LOAD_CA_CERTS, String.valueOf(true));
    return properties;
  }
}
