/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.service.soap.introspection;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.wsdl.BindingOperation;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.OperationType;
import javax.wsdl.Part;
import javax.wsdl.extensions.ElementExtensible;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap12.SOAP12Body;
import javax.wsdl.extensions.soap12.SOAP12Header;

public class OperationDefinition {

  private final BindingOperation bindingOperation;
  private final Operation operation;

  OperationDefinition(BindingOperation bop) {
    this.bindingOperation = bop;
    this.operation = bop.getOperation();
  }

  public Message getInputMessage() {
    return operation.getInput().getMessage();
  }

  public Message getOutputMessage() {
    return operation.getOutput().getMessage();
  }

  public Optional<Part> getInputBodyPart() {
    return getBodyPart(getInputMessage(), bindingOperation.getBindingInput());
  }

  public Optional<Part> getOutputBodyPart() {
    return getBodyPart(getOutputMessage(), bindingOperation.getBindingOutput());
  }

  public List<SoapHeaderAdapter> getInputHeaders() {
    return getHeaderParts(bindingOperation.getBindingInput());
  }

  public List<SoapHeaderAdapter> getOutputHeaders() {
    return getHeaderParts(bindingOperation.getBindingOutput());
  }

  public String getName() {
    return operation.getName();
  }

  public OperationType getType() {
    return operation.getStyle();
  }

  private Optional<Part> getBodyPart(Message message, ElementExtensible bindingType) {
    Map parts = message.getParts();
    if (parts == null || parts.isEmpty()) {
      return empty();
    }
    if (parts.size() == 1) {
      return ofNullable((Part) parts.get(parts.keySet().toArray()[0]));
    }
    return getBodyPartName(bindingType).flatMap(partName -> ofNullable((Part) parts.get(partName)));
  }

  @SuppressWarnings("unchecked")
  private Optional<String> getBodyPartName(ElementExtensible bindingType) {
    List elements = bindingType.getExtensibilityElements();
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

  List<SoapHeaderAdapter> getHeaderParts(ElementExtensible bindingType) {
    List extensible = bindingType.getExtensibilityElements();
    if (extensible != null) {
      return (List<SoapHeaderAdapter>) extensible.stream()
          .filter(e -> e instanceof SOAPHeader || e instanceof SOAP12Header)
          .map(e -> e instanceof SOAPHeader ? new SoapHeaderAdapter((SOAPHeader) e) : new SoapHeaderAdapter((SOAP12Header) e))
          .collect(toList());
    }
    return emptyList();
  }

  public BindingOperation getBindingOperation() {
    return bindingOperation;
  }
}
