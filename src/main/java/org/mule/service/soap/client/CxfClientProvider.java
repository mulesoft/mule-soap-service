/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.client;

import com.google.common.collect.ImmutableList;
import org.apache.cxf.binding.Binding;
import org.apache.cxf.binding.soap.interceptor.CheckFaultInterceptor;
import org.apache.cxf.binding.soap.interceptor.Soap11FaultInInterceptor;
import org.apache.cxf.binding.soap.interceptor.Soap12FaultInInterceptor;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.WrappedOutInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.mule.runtime.extension.api.soap.security.DecryptSecurityStrategy;
import org.mule.runtime.extension.api.soap.security.EncryptSecurityStrategy;
import org.mule.runtime.extension.api.soap.security.SecurityStrategy;
import org.mule.runtime.extension.api.soap.security.SecurityStrategyVisitor;
import org.mule.runtime.extension.api.soap.security.SignSecurityStrategy;
import org.mule.runtime.extension.api.soap.security.TimestampSecurityStrategy;
import org.mule.runtime.extension.api.soap.security.UsernameTokenSecurityStrategy;
import org.mule.runtime.extension.api.soap.security.VerifySignatureSecurityStrategy;
import org.mule.runtime.soap.api.SoapVersion;
import org.mule.runtime.soap.api.client.SoapClientConfiguration;
import org.mule.service.soap.interceptor.NamespaceRestorerStaxInterceptor;
import org.mule.service.soap.interceptor.NamespaceSaverStaxInterceptor;
import org.mule.service.soap.interceptor.OutputMtomSoapAttachmentsInterceptor;
import org.mule.service.soap.interceptor.OutputSoapHeadersInterceptor;
import org.mule.service.soap.interceptor.SoapActionInterceptor;
import org.mule.service.soap.interceptor.StreamClosingInterceptor;
import org.mule.service.soap.security.SecurityStrategyCxfAdapter;
import org.mule.service.soap.security.SecurityStrategyType;
import org.mule.service.soap.security.WssDecryptSecurityStrategyCxfAdapter;
import org.mule.service.soap.security.WssEncryptSecurityStrategyCxfAdapter;
import org.mule.service.soap.security.WssSignSecurityStrategyCxfAdapter;
import org.mule.service.soap.security.WssTimestampSecurityStrategyCxfAdapter;
import org.mule.service.soap.security.WssUsernameTokenSecurityStrategyCxfAdapter;
import org.mule.service.soap.security.WssVerifySignatureSecurityStrategyCxfAdapter;
import org.mule.service.soap.security.callback.CompositeCallbackHandler;
import org.mule.service.soap.transport.SoapServiceTransportFactory;

import javax.security.auth.callback.CallbackHandler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static java.util.Collections.emptyMap;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.cxf.message.Message.MTOM_ENABLED;
import static org.apache.ws.security.handler.WSHandlerConstants.ACTION;
import static org.apache.ws.security.handler.WSHandlerConstants.PW_CALLBACK_REF;

/**
 * Object that creates CXF specific clients based on a {@link SoapClientConfiguration} setting all the required CXF properties.
 * <p>
 * the created client aims to be the CXF client used in the {@link SoapCxfClient}.
 *
 * @since 1.0
 */
class CxfClientProvider {

  private final SoapServiceTransportFactory factory = new SoapServiceTransportFactory();

  Client getClient(SoapClientConfiguration configuration) {
    boolean isMtom = configuration.isMtomEnabled();
    String address = configuration.getAddress();
    SoapVersion version = configuration.getVersion();
    Client client = factory.createClient(address, version.getVersion());
    addSecurityInterceptors(client, getAdaptedSecurities(configuration.getSecurities()));
    addRequestInterceptors(client);
    addResponseInterceptors(client, isMtom);
    client.getEndpoint().put(MTOM_ENABLED, isMtom);
    removeUnnecessaryCxfInterceptors(client);
    return client;
  }

  private List<SecurityStrategyCxfAdapter> getAdaptedSecurities(List<SecurityStrategy> securities) {
    ImmutableList.Builder<SecurityStrategyCxfAdapter> builder = ImmutableList.builder();
    securities.forEach(s -> s.accept(new SecurityStrategyVisitor() {

      @Override
      public void visitEncrypt(EncryptSecurityStrategy encrypt) {
        builder.add(new WssEncryptSecurityStrategyCxfAdapter(encrypt.getKeyStoreConfiguration()));
      }

      @Override
      public void visitDecrypt(DecryptSecurityStrategy decrypt) {
        builder.add(new WssDecryptSecurityStrategyCxfAdapter(decrypt.getKeyStoreConfiguration()));
      }

      @Override
      public void visitUsernameToken(UsernameTokenSecurityStrategy usernameToken) {
        builder.add(new WssUsernameTokenSecurityStrategyCxfAdapter(usernameToken));
      }

      @Override
      public void visitSign(SignSecurityStrategy sign) {
        builder.add(new WssSignSecurityStrategyCxfAdapter(sign.getKeyStoreConfiguration()));
      }

      @Override
      public void visitVerify(VerifySignatureSecurityStrategy verify) {
        WssVerifySignatureSecurityStrategyCxfAdapter adapter =
            verify.getTrustStoreConfiguration().map(WssVerifySignatureSecurityStrategyCxfAdapter::new)
                .orElse(new WssVerifySignatureSecurityStrategyCxfAdapter());
        builder.add(adapter);
      }

      @Override
      public void visitTimestamp(TimestampSecurityStrategy timestamp) {
        builder.add(new WssTimestampSecurityStrategyCxfAdapter(timestamp.getTimeToLeaveInSeconds()));
      }
    }));

    return builder.build();
  }

  private void addSecurityInterceptors(Client client, List<SecurityStrategyCxfAdapter> securityStrategies) {
    Map<String, Object> requestProps = buildSecurityProperties(securityStrategies, SecurityStrategyType.OUTGOING);
    if (!requestProps.isEmpty()) {
      client.getOutInterceptors().add(new WSS4JOutInterceptor(requestProps));
    }

    Map<String, Object> responseProps = buildSecurityProperties(securityStrategies, SecurityStrategyType.INCOMING);
    if (!responseProps.isEmpty()) {
      client.getInInterceptors().add(new WSS4JInInterceptor(responseProps));
    }
  }

  private Map<String, Object> buildSecurityProperties(List<SecurityStrategyCxfAdapter> strategies,
                                                      SecurityStrategyType type) {
    if (strategies.isEmpty()) {
      return emptyMap();
    }

    Map<String, Object> props = new HashMap<>();
    StringJoiner actionsJoiner = new StringJoiner(" ");

    ImmutableList.Builder<CallbackHandler> callbackHandlersBuilder = ImmutableList.builder();
    strategies.stream()
        .filter(s -> s.securityType().equals(type))
        .forEach(s -> {
          props.putAll(s.buildSecurityProperties());
          actionsJoiner.add(s.securityAction());
          s.buildPasswordCallbackHandler().ifPresent(callbackHandlersBuilder::add);
        });

    List<CallbackHandler> handlers = callbackHandlersBuilder.build();
    if (!handlers.isEmpty()) {
      props.put(PW_CALLBACK_REF, new CompositeCallbackHandler(handlers));
    }

    String actions = actionsJoiner.toString();
    if (isNotBlank(actions)) {
      props.put(ACTION, actions);
    }

    // This Map needs to be mutable, cxf will add properties if needed.
    return props;
  }

  private void addRequestInterceptors(Client client) {
    List<Interceptor<? extends Message>> outInterceptors = client.getOutInterceptors();
    outInterceptors.add(new SoapActionInterceptor());
  }

  private void addResponseInterceptors(Client client, boolean mtomEnabled) {
    List<Interceptor<? extends Message>> inInterceptors = client.getInInterceptors();
    inInterceptors.add(new NamespaceRestorerStaxInterceptor());
    inInterceptors.add(new NamespaceSaverStaxInterceptor());
    inInterceptors.add(new StreamClosingInterceptor());
    inInterceptors.add(new CheckFaultInterceptor());
    inInterceptors.add(new OutputSoapHeadersInterceptor());
    inInterceptors.add(new SoapActionInterceptor());
    if (mtomEnabled) {
      inInterceptors.add(new OutputMtomSoapAttachmentsInterceptor());
    }
  }

  private void removeUnnecessaryCxfInterceptors(Client client) {
    Binding binding = client.getEndpoint().getBinding();
    removeInterceptor(binding.getOutInterceptors(), WrappedOutInterceptor.class.getName());
    removeInterceptor(binding.getInInterceptors(), Soap11FaultInInterceptor.class.getName());
    removeInterceptor(binding.getInInterceptors(), Soap12FaultInInterceptor.class.getName());
    removeInterceptor(binding.getInInterceptors(), CheckFaultInterceptor.class.getName());
  }

  private void removeInterceptor(List<Interceptor<? extends Message>> inInterceptors, String name) {
    inInterceptors.removeIf(i -> i instanceof PhaseInterceptor && ((PhaseInterceptor) i).getId().equals(name));
  }
}
