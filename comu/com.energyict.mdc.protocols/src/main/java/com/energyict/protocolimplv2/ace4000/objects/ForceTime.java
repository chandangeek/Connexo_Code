/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.ace4000.objects;

import com.energyict.protocolimplv2.ace4000.xml.XMLTags;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Date;

/**
 * @author gna
 */
public class ForceTime extends AbstractActarisObject {

    public ForceTime(ObjectFactory of) {
        super(of);
    }

    private Date newTime = null;

    /**
     * Force the Meter time to the System time
     */
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

        String newDate = getHexDate(getNewTime());

        Element ft = doc.createElement(XMLTags.FORCETIME);
        md.appendChild(ft);
        Element t1 = doc.createElement(XMLTags.TIME1);
        t1.setTextContent("00000000");
        ft.appendChild(t1);
        Element t2 = doc.createElement(XMLTags.TIME2);
        t2.setTextContent(newDate);
        ft.appendChild(t2);
        Element t3 = doc.createElement(XMLTags.TIME3);
        t3.setTextContent(newDate);
        ft.appendChild(t3);

        String msg = convertDocumentToString(doc);
        return (msg.substring(msg.indexOf("?>") + 2));
    }

    protected void parse(Element mdElement) {
        //This results in <DT> tags, which are parsed in the DateTime class
    }

    public void setNewTime(Date newTime) {
        this.newTime = newTime;
    }

    public Date getNewTime() {
        if (this.newTime == null) {
            return new Date();
        } else {
            return newTime;
        }
    }
}