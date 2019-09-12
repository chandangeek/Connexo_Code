package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServiceCallRelatedObjectTypeImpl;
import com.elster.jupiter.util.HasId;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface WebServiceCallRelatedObjectType extends HasId {

    String getValue();

    WebServiceCallRelatedObjectTypeImpl init(String typeDomain, String key, String value);

    void save();

}
