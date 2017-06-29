/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.introspection;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import org.mule.metadata.xml.SchemaCollector;
import org.mule.runtime.soap.api.exception.InvalidWsdlException;
import org.mule.runtime.soap.api.transport.NullTransportResourceLocator;
import org.mule.runtime.soap.api.transport.TransportResourceLocator;
import org.mule.service.soap.metadata.TypeIntrospecterDelegate;
import com.ibm.wsdl.extensions.schema.SchemaSerializer;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.mime.MIMEPart;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.wsdl.extensions.soap12.SOAP12Body;
import javax.wsdl.extensions.soap12.SOAP12Operation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLLocator;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Parses a WSDL file and for a given service name and port name introspecting all the operations and components for the given
 * set.
 *
 * @since 1.0
 */
@SuppressWarnings("unchecked")
public class WsdlDefinition {

  private static final String DOCUMENT_STYLE = "document";
  private static final String RPC_STYLE = "rpc";

  private final WsdlSchemasCollector schemaCollector;
  private final Definition definition;
  private final Service service;
  private final Port port;

  public WsdlDefinition(String wsdlLocation, String serviceName, String portName) {
    this(wsdlLocation, serviceName, portName, new NullTransportResourceLocator());
  }

  public WsdlDefinition(String wsdlLocation, String serviceName, String portName, TransportResourceLocator locator) {
    validateBlankString(wsdlLocation, "WSDL Location");
    validateNotNull(locator, "ResourceLocator cannot be null");
    this.definition = parseWsdl(new WsdlLocator(wsdlLocation, locator));
    this.service = findService(serviceName);
    this.port = findPort(portName);
    this.schemaCollector = new WsdlSchemasCollector(definition);
  }

  private Service findService(String serviceName) {
    validateBlankString(serviceName, "service name");
    Service service = definition.getService(new QName(definition.getTargetNamespace(), serviceName));
    validateNotNull(service, "The service name [" + serviceName + "] was not found in the current wsdl file.");
    return service;
  }

  private Port findPort(String portName) {
    validateBlankString(portName, "port name");
    Port port = service.getPort(portName.trim());
    validateNotNull(port, "The port name [" + portName + "] was not found in the current wsdl file.");
    return port;
  }

  public List<String> getOperationNames() {
    List<BindingOperation> bindingOperations = (List<BindingOperation>) port.getBinding().getBindingOperations();
    return bindingOperations.stream().map(BindingOperation::getName).collect(toList());
  }

  public Operation getOperation(String operationName) {
    validateBlankString(operationName, "operation name");
    Operation operation = port.getBinding().getPortType().getOperation(operationName, null, null);
    validateNotNull(operation, "The operation name [" + operationName + "] was not found in the current wsdl file.");
    return operation;
  }

  public BindingOperation getBindingOperation(String operationName) {
    validateBlankString(operationName, "operation name");
    BindingOperation operation = port.getBinding().getBindingOperation(operationName, null, null);
    validateNotNull(operation, "The binding operation name [" + operationName + "] was not found in the current wsdl file.");
    return operation;
  }

  public Optional<Part> getBodyPart(String operation, TypeIntrospecterDelegate delegate) {
    BindingOperation bindingOperation = getBindingOperation(operation);
    Message message = delegate.getMessage(bindingOperation.getOperation());
    Map parts = message.getParts();
    if (parts == null || parts.isEmpty()) {
      return empty();
    }
    if (parts.size() == 1) {
      return ofNullable((Part) parts.get(parts.keySet().toArray()[0]));
    }
    return getBodyPartName(bindingOperation, delegate).flatMap(partName -> ofNullable((Part) parts.get(partName)));
  }

  @SuppressWarnings("unchecked")
  private Optional<String> getBodyPartName(BindingOperation bindingOperation, TypeIntrospecterDelegate delegate) {
    List elements = delegate.getBindingType(bindingOperation).getExtensibilityElements();
    if (elements != null) {
      Optional<List> bodyParts = elements.stream()
          .filter(e -> e instanceof SOAPBody || e instanceof SOAP12Body)
          .map(e -> e instanceof SOAPBody ? ((SOAPBody) e).getParts() : ((SOAP12Body) e).getParts())
          .map(parts -> parts == null ? emptyList() : parts)
          .findFirst();

      if (bodyParts.isPresent() && !bodyParts.get().isEmpty()) {
        return ofNullable((String) bodyParts.get().get(0));
      }
    }
    return empty();
  }

  public Fault getFault(Operation operation, String faultName) {
    validateBlankString(faultName, "fault name");
    Fault fault = operation.getFault(faultName);
    validateNotNull(fault, "The fault name [" + faultName + "] was not found in the current wsdl file.");
    return fault;
  }

  public Message getMessage(QName qName) {
    return definition.getMessage(qName);
  }

  public SchemaCollector getSchemas() {
    return schemaCollector.collect();
  }

  public Service getService() {
    return service;
  }

  public Port getPort() {
    return port;
  }

  /**
   * Tries to find the address where the web service is located.
   */
  public Optional<String> getSoapAddress() {
    String address = null;
    if (port != null) {
      for (Object element : port.getExtensibilityElements()) {
        if (element instanceof SOAPAddress) {
          address = ((SOAPAddress) element).getLocationURI();
          break;
        } else if (element instanceof SOAP12Address) {
          address = ((SOAP12Address) element).getLocationURI();
          break;
        } else if (element instanceof HTTPAddress) {
          address = ((HTTPAddress) element).getLocationURI();
          break;
        }
      }
    }
    return ofNullable(address);
  }

  public boolean isRpcStyle() {
    return isWsdlStyle(RPC_STYLE);
  }

  public boolean isDocumentStyle() {
    return isWsdlStyle(DOCUMENT_STYLE);
  }

  /**
   * Checks if the provided {@code style} is the same style as the introspected WSDL.
   */
  private boolean isWsdlStyle(String style) {

    List elements = port.getBinding().getExtensibilityElements();
    // Looking the style value in the binding.
    Optional<String> bindingStyle = elements.stream()
        .filter(e -> e instanceof SOAP12Binding || e instanceof SOAPBinding)
        .map(e -> e instanceof SOAP12Binding ? ((SOAP12Binding) e).getStyle() : ((SOAPBinding) e).getStyle())
        .filter(Objects::nonNull)
        .findAny();

    List bindingOperations = port.getBinding().getBindingOperations();
    if (!bindingStyle.isPresent() && !bindingOperations.isEmpty()) {
      // if not defined in the binding, one operation is taken to see if the style is defined there.
      BindingOperation bop = (BindingOperation) bindingOperations.get(0);
      bindingStyle = bop.getExtensibilityElements().stream()
          .filter(e -> e instanceof SOAP12Operation || e instanceof SOAPOperation)
          .map(e -> e instanceof SOAPOperation ? ((SOAPOperation) e).getStyle() : ((SOAP12Operation) e).getStyle())
          .filter(Objects::nonNull)
          .findAny();
    }

    // if no style was found, the default one is DOCUMENT
    return bindingStyle.orElse(DOCUMENT_STYLE).equalsIgnoreCase(style);
  }

  /**
   * Given a Wsdl location (either local or remote) it will fetch the definition. If the definition cannot be created, then
   * an exception will be raised
   *
   * @param locator a {@link WSDLLocator} used to locate the WSDL and it referenced resources.
   */
  private Definition parseWsdl(WSDLLocator locator) {
    try {
      WSDLFactory factory = WSDLFactory.newInstance();
      ExtensionRegistry registry = initExtensionRegistry(factory);
      WSDLReader wsdlReader = factory.newWSDLReader();
      wsdlReader.setFeature("javax.wsdl.verbose", false);
      wsdlReader.setFeature("javax.wsdl.importDocuments", true);
      wsdlReader.setExtensionRegistry(registry);
      return wsdlReader.readWSDL(locator);
    } catch (WSDLException e) {
      throw new InvalidWsdlException(format("Error processing WSDL file [%s]: %s", locator.getBaseURI(), e.getMessage()), e);
    }
  }

  private ExtensionRegistry initExtensionRegistry(WSDLFactory factory) throws WSDLException {
    ExtensionRegistry registry = factory.newPopulatedExtensionRegistry();
    registry.registerSerializer(Types.class, new QName("http://www.w3.org/2001/XMLSchema", "schema"), new SchemaSerializer());

    // these will replace whatever may have already been registered
    // in these places, but there's no good way to check what was
    // there before.
    QName header = new QName("http://schemas.xmlsoap.org/wsdl/soap/", "header");
    registry.registerDeserializer(MIMEPart.class, header, registry.queryDeserializer(BindingInput.class, header));
    registry.registerSerializer(MIMEPart.class, header, registry.querySerializer(BindingInput.class, header));

    // get the original classname of the SOAPHeader
    // implementation that was stored in the registry.
    Class<? extends ExtensibilityElement> clazz = registry.createExtension(BindingInput.class, header).getClass();
    registry.mapExtensionTypes(MIMEPart.class, header, clazz);
    return registry;
  }

  private void validateNotNull(Object paramValue, String errorMessage) {
    if (paramValue == null) {
      throw new IllegalArgumentException(errorMessage);
    }
  }

  private void validateBlankString(String paramValue, String paramName) {
    if (isBlank(paramValue)) {
      throw new IllegalArgumentException("The [" + paramName + "] can not be blank nor null.");
    }
  }
}
