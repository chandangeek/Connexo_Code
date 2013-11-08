package com.energyict.protocolimplv2.ace4000.objects;

import org.w3c.dom.Element;

/**
 * @author khe
 */
public class Reject extends AbstractActarisObject {

    public Reject(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    protected void parse(Element element) {
        reason = Integer.parseInt(element.getTextContent(), 16);         //Parent class contains description method
    }

    @Override
    protected String prepareXML() {
        return "";        //A Reject is never sent as a request.
    }
}