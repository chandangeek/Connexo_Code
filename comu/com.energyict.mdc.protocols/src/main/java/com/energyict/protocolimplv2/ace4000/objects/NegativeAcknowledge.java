/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.ace4000.objects;

import com.energyict.protocolimplv2.ace4000.xml.XMLTags;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author khe
 */
public class NegativeAcknowledge extends AbstractActarisObject {

    private int failedTrackingId = 0;

    public NegativeAcknowledge(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    protected void parse(Element element) {

        NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Element subElement = (Element) list.item(i);
            if (subElement.getNodeName().equalsIgnoreCase(XMLTags.TRACKER)) {
                failedTrackingId = Integer.parseInt(subElement.getTextContent(), 16);
            }
            if (subElement.getNodeName().equalsIgnoreCase(XMLTags.REASON)) {
                reason = Integer.parseInt(subElement.getTextContent(), 16);
            }
        }
    }

    public int getFailedTrackingId() {
        return failedTrackingId;
    }

    public String prepareXML() {
        return "";      //We don't send NACK's
    }
}