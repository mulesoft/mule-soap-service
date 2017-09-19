/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.service.soap.metadata;

import org.mule.metadata.api.TypeLoader;
import org.mule.service.soap.introspection.OperationDefinition;
import org.mule.service.soap.introspection.ServiceDefinition;

/**
 * {@link BodyMetadataResolver} implementation for the output soap body.
 *
 * @since 1.0
 */
public final class InputBodyMetadataResolver extends BodyMetadataResolver {

  InputBodyMetadataResolver(ServiceDefinition definition, TypeLoader loader) {
    super(definition, loader, OperationDefinition::getInputBodyPart);
  }
}
