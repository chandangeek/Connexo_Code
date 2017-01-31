/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.ace4000.objects;

import com.energyict.protocolimplv2.ace4000.xml.XMLTags;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author khe
 */
public class FirmwareUpgrade extends AbstractActarisObject {

    private String path = "";
    private int jarSize = 0;
    private int jadSize = 0;

    public void setJadSize(int jadSize) {
        this.jadSize = jadSize;
    }

    public void setJarSize(int jarSize) {
        this.jarSize = jarSize;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public FirmwareUpgrade(ObjectFactory of) {
        super(of);
    }

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

        Element fwUpgrade = doc.createElement(XMLTags.FWUPGRADE);
        md.appendChild(fwUpgrade);
        Element pathElement = doc.createElement(XMLTags.FW_PATH);
        pathElement.setTextContent(path);
        fwUpgrade.appendChild(pathElement);
        Element jarSizeElement = doc.createElement(XMLTags.FW_JAR_SIZE);
        jarSizeElement.setTextContent(Integer.toHexString(jarSize));
        fwUpgrade.appendChild(jarSizeElement);
        Element jadSizeElement = doc.createElement(XMLTags.FW_JAD_SIZE);
        jadSizeElement.setTextContent(Integer.toHexString(jadSize));
        fwUpgrade.appendChild(jadSizeElement);

        String msg = convertDocumentToString(doc);
        return (msg.substring(msg.indexOf("?>") + 2));
    }

    protected void parse(Element mdElement) {
        //Only ACK is received
    }
}