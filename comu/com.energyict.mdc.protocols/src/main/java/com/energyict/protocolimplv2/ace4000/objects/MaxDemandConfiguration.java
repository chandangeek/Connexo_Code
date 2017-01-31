/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.ace4000.objects;

import com.energyict.protocolimplv2.ace4000.xml.XMLTags;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MaxDemandConfiguration extends AbstractActarisObject {

    public MaxDemandConfiguration(ObjectFactory of) {
        super(of);
    }

    @Override
    protected void parse(Element element) {
        //Only ack or nack is sent back
    }

    private int register;
    private int numberOfSubIntervals;
    private int subIntervalDuration;

    public void setNumberOfSubIntervals(int numberOfSubIntervals) {
        this.numberOfSubIntervals = numberOfSubIntervals;
    }

    public void setRegister(int register) {
        this.register = register;
    }

    public void setSubIntervalDuration(int subIntervalDuration) {
        this.subIntervalDuration = subIntervalDuration;
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
        Element displayConfigElement = doc.createElement(XMLTags.MAXDEMANDCONFIG);
        cf.appendChild(displayConfigElement);

        Element registerElement = doc.createElement(XMLTags.MXDREG);
        registerElement.setTextContent(Integer.toString(register));
        displayConfigElement.appendChild(registerElement);

        Element numberOfIntervalsElement = doc.createElement(XMLTags.MXDSUBI_NUMBER);
        numberOfIntervalsElement.setTextContent(Integer.toString(numberOfSubIntervals, 16));
        displayConfigElement.appendChild(numberOfIntervalsElement);

        Element intervalDurationElement = doc.createElement(XMLTags.MXDSUBI_DURATION);
        intervalDurationElement.setTextContent(Integer.toString(subIntervalDuration, 16));
        displayConfigElement.appendChild(intervalDurationElement);

        String msg = convertDocumentToString(doc);
        return (msg.substring(msg.indexOf("?>") + 2));
    }
}