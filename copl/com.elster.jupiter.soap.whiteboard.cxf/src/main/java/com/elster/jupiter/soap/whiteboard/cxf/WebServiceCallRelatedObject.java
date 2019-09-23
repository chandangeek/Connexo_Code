package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServiceCallRelatedObjectImpl;
import com.elster.jupiter.util.HasId;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface WebServiceCallRelatedObject extends HasId {

    String getValue();

    String getKey();

    WebServiceCallRelatedObjectImpl init(String key, String value);

    void save();

}
