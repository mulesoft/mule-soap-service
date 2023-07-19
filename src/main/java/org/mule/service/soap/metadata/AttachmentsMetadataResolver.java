/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.metadata;

import static org.mule.metadata.api.utils.MetadataTypeUtils.getLocalPart;
import static org.mule.service.soap.util.SoapServiceMetadataTypeUtils.getAttachmentFields;

import org.mule.metadata.api.TypeLoader;
import org.mule.metadata.api.builder.ObjectTypeBuilder;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.api.model.ObjectFieldType;
import org.mule.metadata.api.model.ObjectType;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.wsdl.parser.model.PortModel;
import org.mule.wsdl.parser.model.operation.OperationModel;
import org.mule.wsdl.parser.model.operation.OperationType;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.wsdl.Message;
import javax.wsdl.Part;
import javax.xml.namespace.QName;

/**
 * Handles the dynamic {@link MetadataType} resolution for the SOAP attachments of a web service operation.
 *
 * @since 1.0
 */
abstract class AttachmentsMetadataResolver extends NodeMetadataResolver {

  private final Function<OperationModel, Optional<Message>> messageRetriever;

  AttachmentsMetadataResolver(PortModel port,
                              TypeLoader loader,
                              Function<OperationModel, Optional<Part>> partRetriever,
                              Function<OperationModel, Optional<Message>> messageRetriever) {
    super(port, loader, partRetriever);
    this.messageRetriever = messageRetriever;
  }

  @Override
  public MetadataType getMetadata(String operation) throws MetadataResolvingException {
    Part bodyPart = getBodyPart(operation);
    MetadataType bodyType = buildPartMetadataType(bodyPart);
    List<ObjectFieldType> attachments = getAttachmentFields(bodyType);
    if (attachments.isEmpty()) {
      return getMultipartAttachments(operation, bodyPart).orElse(nullType);
    }
    // TODO(MULE-15275): This piece of code should be removed when soap with attachments are no longer parsed.
    ObjectTypeBuilder type = typeBuilder.objectType();
    attachments.forEach(attachment -> type.addField()
        .key(getLocalPart(attachment))
        .value(attachment.getValue()));
    return type.build();
  }

  // TODO(MULE-15275): Move this to the parser.
  private Optional<MetadataType> getMultipartAttachments(String operation, Part bodyPart) {
    Message message = this.messageRetriever.apply(this.port.getOperation(operation)).orElse(null);
    if (message != null) {
      Map<String, Part> parts = message.getParts();
      if (parts != null) {
        ObjectTypeBuilder type = typeBuilder.objectType();
        parts.forEach((partName, partObject) -> {
          if (!bodyPart.getName().equals(partName)) {
            QName typeName = partObject.getTypeName();
            if (typeName != null && typeName.toString().toLowerCase().contains("binary")) {
              type.addField().key(partName).value(typeBuilder.binaryType().build());
            }
          }
        });
        ObjectType result = type.build();
        if (result.getFields().isEmpty()) {
          return Optional.empty();
        }
        return Optional.of(result);
      }
    }
    return Optional.empty();
  }
}
