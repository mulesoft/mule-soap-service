/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.runtime.wss;

import static org.mule.test.allure.AllureConstants.WscFeature.WSC_EXTENSION;
import static java.util.Collections.singletonList;
import org.mule.runtime.extension.api.soap.security.DecryptSecurityStrategy;
import org.mule.runtime.extension.api.soap.security.SecurityStrategy;
import org.mule.runtime.extension.api.soap.security.config.WssKeyStoreConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(WSC_EXTENSION)
@Story("WSS")
public class WssDecryptTestCase extends AbstractWebServiceSecurityTestCase {

  @Override
  protected Interceptor buildOutInterceptor() {
    final Map<String, Object> props = new HashMap<>();
    props.put("action", "Encrypt");
    props.put("encryptionUser", "s1as");

    final String encryptionPropRefId = "securityProperties";
    props.put("encryptionPropRefId", encryptionPropRefId);
    final Properties securityProperties = new Properties();
    securityProperties.put("org.apache.ws.security.crypto.merlin.truststore.type", "jks");
    securityProperties.put("org.apache.ws.security.crypto.merlin.truststore.password", "changeit");
    securityProperties.put("org.apache.ws.security.crypto.merlin.truststore.file", "security/ssltest-cacerts.jks");
    props.put(encryptionPropRefId, securityProperties);

    return new WSS4JOutInterceptor(props);
  }

  @Override
  protected List<SecurityStrategy> getSecurityStrategies() {
    return singletonList(new DecryptSecurityStrategy(
                                                     new WssKeyStoreConfiguration("s1as", "changeit", "changeit",
                                                                                  "security/ssltest-keystore.jks", "jks")));
  }
}
