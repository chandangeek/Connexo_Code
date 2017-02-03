package com.energyict.protocolimpl.edf.messages.objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class ComplexCosemObject {

    public ComplexCosemObject() {
        super();
    }

    public ComplexCosemObject(Element element) {
        super();
    }

    public abstract Element generateXMLElement(Document document);
}
