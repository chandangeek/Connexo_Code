/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.ace4000.objects;

import com.energyict.protocolimplv2.ace4000.xml.XMLTags;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class LoadProfileConfiguration extends AbstractActarisObject {

    public LoadProfileConfiguration(ObjectFactory of) {
        super(of);
    }

    @Override
    protected void parse(Element element) {
        //Only ack or nack is sent back
    }

    private int interval;
    private int maxNumberOfRecords;
    private int enable;

    public void setEnable(int enable) {
        this.enable = enable;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public void setMaxNumberOfRecords(int maxNumberOfRecords) {
        this.maxNumberOfRecords = maxNumberOfRecords;
    }

    @Override
    protected String prepareXML() {
        Document doc = createDomDocument();

        Element root = doc.createElement(XMLTags.MPULL);
        doc.appendChild(root);
        Element md = doc.createElement(XMLTags.METERDATA);
        root.appendChild(md);
        Element s = doc.createElement(XMLTags.SERIALNUMBER);
        s.setTextContent(getObjectFactory().getAce4000().getSerialNumber());
        md.appendChild(s);
        Element t = doc.createElement(XMLTags.TRACKER);
        t.setTextContent(Integer.toString(getTrackingID(), 16));
        md.appendChild(t);

        Element cf = doc.createElement(XMLTags.CONFIGURATION);
        md.appendChild(cf);
        Element displayConfigElement = doc.createElement(XMLTags.LPCONFIG);
        cf.appendChild(displayConfigElement);

        Element enableElement = doc.createElement(XMLTags.LPENABLE);
        enableElement.setTextContent(Integer.toString(enable));
        displayConfigElement.appendChild(enableElement);

        Element intervalElement = doc.createElement(XMLTags.LPINTERVAL);
        intervalElement.setTextContent(Integer.toString(interval, 16));
        displayConfigElement.appendChild(intervalElement);

        Element maxNumberElement = doc.createElement(XMLTags.LPMAXNUMBER);
        maxNumberElement.setTextContent(Integer.toString(maxNumberOfRecords, 16));
        displayConfigElement.appendChild(maxNumberElement);

        String msg = convertDocumentToString(doc);
        return (msg.substring(msg.indexOf("?>") + 2));
    }
}