/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.runtime.wss;


import static java.util.Collections.singletonList;
import static org.mule.test.allure.AllureConstants.WscFeature.WSC_EXTENSION;

import org.mule.runtime.extension.api.soap.security.EncryptSecurityStrategy;
import org.mule.runtime.extension.api.soap.security.SecurityStrategy;
import org.mule.runtime.extension.api.soap.security.config.WssKeyStoreConfiguration;
import org.mule.service.soap.service.EncryptPasswordCallback;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.wss4j.common.crypto.Merlin;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Feature(WSC_EXTENSION)
@Story("WSS")
public class WssEncryptTestCase extends AbstractWebServiceSecurityTestCase {

  @Override
  protected Interceptor buildInInterceptor() {
    final Map<String, Object> props = new HashMap<>();
    props.put("action", "Encrypt");
    props.put("passwordCallbackClass", EncryptPasswordCallback.class.getName());
    final String decryptionPropRefId = "securityProperties";
    props.put("decryptionPropRefId", decryptionPropRefId);
    final Properties securityProperties = new Properties();
    securityProperties.put("org.apache.ws.security.crypto.provider", Merlin.class.getName());
    securityProperties.put("org.apache.ws.security.crypto.merlin.keystore.type", "jks");
    securityProperties.put("org.apache.ws.security.crypto.merlin.keystore.password", "changeit");
    securityProperties.put("org.apache.ws.security.crypto.merlin.keystore.private.password", "changeit");
    securityProperties.put("org.apache.ws.security.crypto.merlin.keystore.alias", "s1as");
    securityProperties.put("org.apache.ws.security.crypto.merlin.keystore.file", "security/ssltest-keystore.jks");
    props.put(decryptionPropRefId, securityProperties);
    return new WSS4JInInterceptor(props);
  }

  @Override
  protected List<SecurityStrategy> getSecurityStrategies() {
    WssKeyStoreConfiguration keyStoreConfig = new WssKeyStoreConfiguration("s1as", "changeit", "security/ssltest-cacerts.jks");
    return singletonList(new EncryptSecurityStrategy(keyStoreConfig));
  }
}
