package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServiceCallRelatedAttributeImpl;
import com.elster.jupiter.util.HasId;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface WebServiceCallRelatedAttribute extends HasId {

    String getValue();

    String getKey();

    WebServiceCallRelatedAttributeImpl init(String key, String value);
}
