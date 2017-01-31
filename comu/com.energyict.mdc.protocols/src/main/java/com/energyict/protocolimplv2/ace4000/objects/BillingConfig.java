/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.ace4000.objects;

import com.energyict.protocolimplv2.ace4000.xml.XMLTags;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author gna
 */
public class BillingConfig extends AbstractActarisObject {

    private int enabled = -1;
    private int interval = -1;
    private int numOfRecs = -1;

    public BillingConfig(ObjectFactory of) {
        super(of);
    }

    public String prepareXML() {
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
        Element ps = doc.createElement(XMLTags.BILLINGCONF);
        cf.appendChild(ps);
        Element enable = doc.createElement(XMLTags.BILLENABLE);
        enable.setTextContent(Integer.toString(enabled, 16));
        ps.appendChild(enable);
        Element bi = doc.createElement(XMLTags.BILLINT);
        bi.setTextContent(Integer.toString(interval, 16));
        ps.appendChild(bi);
        Element bn = doc.createElement(XMLTags.BILLNUMB);
        bn.setTextContent(Integer.toString(numOfRecs, 16));
        ps.appendChild(bn);

        String msg = convertDocumentToString(doc);
        return (msg.substring(msg.indexOf("?>") + 2));
    }

    public void parse(Element mdElement) {
        NodeList list = mdElement.getChildNodes();

        for (int i = 0; i < list.getLength(); i++) {
            Element element = (Element) list.item(i);

            if (element.getNodeName().equalsIgnoreCase(XMLTags.BILLENABLE)) {
                setEnabled(Integer.parseInt(element.getTextContent()));
            }
            if (element.getNodeName().equalsIgnoreCase(XMLTags.BILLINT)) {
                setInterval(Integer.parseInt(element.getTextContent(), 16));
            }
            if (element.getNodeName().equalsIgnoreCase(XMLTags.BILLNUMB)) {
                setNumOfRecs(Integer.parseInt(element.getTextContent(), 16));
            }
        }
    }

    public int getEnabled() {
        return enabled;
    }

    protected void setEnabled(int enabled) {
        this.enabled = enabled;
    }

    public int getInterval() {
        return interval;
    }

    protected void setInterval(int interval) {
        this.interval = interval;
    }

    protected int getNumOfRecs() {
        return numOfRecs;
    }

    protected void setNumOfRecs(int numOfRecs) {
        this.numOfRecs = numOfRecs;
    }
}