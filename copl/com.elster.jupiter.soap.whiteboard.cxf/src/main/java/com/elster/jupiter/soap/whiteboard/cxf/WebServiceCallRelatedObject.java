package com.elster.jupiter.soap.whiteboard.cxf;

//import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServiceCallRelatedObjectTypeImpl;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface WebServiceCallRelatedObject {
    long getId();
    Optional<WebServiceCallOccurrence> getOccurrence();
    Optional<WebServiceCallRelatedObjectType> getType();
    void save();
}
