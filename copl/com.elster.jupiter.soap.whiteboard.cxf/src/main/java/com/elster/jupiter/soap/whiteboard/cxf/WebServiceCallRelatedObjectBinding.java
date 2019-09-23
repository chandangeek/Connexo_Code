package com.elster.jupiter.soap.whiteboard.cxf;

//import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServiceCallRelatedObjectTypeImpl;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface WebServiceCallRelatedObjectBinding {
    long getId();
    Optional<WebServiceCallOccurrence> getOccurrence();
    Optional<WebServiceCallRelatedObject> getType();
    void save();
}
