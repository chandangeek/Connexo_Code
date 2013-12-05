package com.energyict.protocolimplv2.ace4000.objects;

import com.energyict.protocolimplv2.ace4000.xml.XMLTags;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Copyrights EnergyICT
 * Date: 10/08/11
 * Time: 13:18
 */
public class DisplayConfiguration extends AbstractActarisObject {

    public DisplayConfiguration(ObjectFactory of) {
        super(of);
    }

    @Override
    protected void parse(Element element) {
        //Only ack or nack is sent back
    }

    private int resolutionCode = 0;
    private String sequence = "";
    private int interval = 2;

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public void setResolutionCode(int resolutionCode) {
        this.resolutionCode = resolutionCode;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
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
        Element displayConfigElement = doc.createElement(XMLTags.DISPLAYCONFIG);
        cf.appendChild(displayConfigElement);

        Element resolutionElement = doc.createElement(XMLTags.RESOLUTION);
        resolutionElement.setTextContent(Integer.toString(resolutionCode));
        displayConfigElement.appendChild(resolutionElement);

        Element sequenceElement = doc.createElement(XMLTags.SEQUENCE);
        sequenceElement.setTextContent(sequence);
        displayConfigElement.appendChild(sequenceElement);

        Element intervalElement = doc.createElement(XMLTags.INTERVAL);
        intervalElement.setTextContent(Integer.toString(interval, 16));
        displayConfigElement.appendChild(intervalElement);

        String msg = convertDocumentToString(doc);
        return (msg.substring(msg.indexOf("?>") + 2));
    }
}