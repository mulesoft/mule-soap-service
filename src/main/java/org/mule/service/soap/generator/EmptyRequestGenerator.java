/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.generator;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import org.mule.metadata.api.TypeLoader;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.api.model.ObjectFieldType;
import org.mule.runtime.soap.api.exception.BadRequestException;
import org.mule.service.soap.util.SoapServiceMetadataTypeUtils;
import org.mule.wsdl.parser.model.PortModel;
import org.mule.wsdl.parser.model.operation.OperationModel;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.wsdl.Message;
import javax.wsdl.Part;
import javax.xml.namespace.QName;

/**
 * Enables the construction of request bodies for web service operations that don't require input parameters.
 *
 * @since 1.0
 */
final class EmptyRequestGenerator {

  private static final String REQUIRED_PARAMS_ERROR_MASK =
      "Cannot build default body request for operation [%s]%s, the operation requires input parameters";

  /**
   * SOAP request mask for operations without input parameters
   */
  private static final String NO_PARAMS_SOAP_BODY_CALL_MASK = "<ns:%s xmlns:ns=\"%s\"/>";
  private final PortModel port;
  private final TypeLoader loader;

  public EmptyRequestGenerator(PortModel port, TypeLoader loader) {
    this.port = port;
    this.loader = loader;
  }

  /**
   * Generates a request body for an operation that don't require input parameters, if the required XML in the body is just one
   * constant element.
   */
  String generateRequest(String operationName) {

    OperationModel operation = port.getOperation(operationName);
    Optional<Part> part = getSinglePart(operation.getInputParts(), operation.getInputMessage());

    if (!part.isPresent()) {
      throw new BadRequestException(format(REQUIRED_PARAMS_ERROR_MASK, operationName,
                                           " there is no single part in the input message"));
    }

    if (part.get().getElementName() == null) {
      throw new BadRequestException(format(REQUIRED_PARAMS_ERROR_MASK, operationName,
                                           " there is one message body part but no does not have an element defined"));
    }

    Part bodyPart = part.get();

    if (isOperationWithRequiredParameters(loader, bodyPart)) {
      // operation has required parameters
      throw new BadRequestException(format(REQUIRED_PARAMS_ERROR_MASK, operationName, ""));
    }

    // There is a single part with an element defined and it does not require parameters
    QName element = bodyPart.getElementName();
    return format(NO_PARAMS_SOAP_BODY_CALL_MASK, element.getLocalPart(), element.getNamespaceURI());
  }

  private boolean isOperationWithRequiredParameters(TypeLoader loader, Part part) {
    // Find the body type
    Optional<MetadataType> bodyType = loader.load(part.getElementName().toString());
    if (bodyType.isPresent()) {
      Collection<ObjectFieldType> operationFields = SoapServiceMetadataTypeUtils.getOperationType(bodyType.get()).getFields();
      return !operationFields.isEmpty();
    }
    return false;
  }

  /**
   * Finds the part of the input message that must be used in the SOAP body, if the operation requires only one part.
   *
   * @param soapBodyParts the body parts discovered in the binding type
   * @param inputMessage  the input {@link Message} of the operation.
   */
  private Optional<Part> getSinglePart(List<String> soapBodyParts, Message inputMessage) {
    if (soapBodyParts.isEmpty()) {
      Map parts = inputMessage.getParts();
      if (parts.size() == 1) {
        return ofNullable((Part) parts.values().iterator().next());
      }
    } else {
      if (soapBodyParts.size() == 1) {
        String partName = soapBodyParts.get(0);
        return ofNullable(inputMessage.getPart(partName));
      }
    }
    return empty();
  }
}
