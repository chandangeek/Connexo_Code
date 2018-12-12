/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.whiteboard;

import aQute.bnd.annotation.ConsumerType;
import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface ReferenceResolver {
    Optional<ReferenceInfo> resolve(Object object);
}
