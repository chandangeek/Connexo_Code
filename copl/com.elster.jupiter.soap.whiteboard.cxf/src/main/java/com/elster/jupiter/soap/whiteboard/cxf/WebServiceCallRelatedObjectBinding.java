/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.soap.whiteboard.cxf;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface WebServiceCallRelatedObjectBinding {
    long getId();
    Optional<WebServiceCallOccurrence> getOccurrence();
    Optional<WebServiceCallRelatedObject> getType();
    void save();
}
