package com.elster.jupiter.rest.whiteboard;

import aQute.bnd.annotation.ConsumerType;

import java.util.Optional;

@ConsumerType
public interface SpecificReferenceResolver {
    Optional<ReferenceInfo> resolve(Object object);
}
