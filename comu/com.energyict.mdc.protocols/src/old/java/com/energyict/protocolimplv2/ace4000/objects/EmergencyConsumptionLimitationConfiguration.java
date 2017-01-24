package com.energyict.protocolimplv2.ace4000.objects;

import com.energyict.protocolimplv2.ace4000.xml.XMLTags;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Copyrights EnergyICT
 * Date: 10/08/11
 * Time: 13:18
 */
public class EmergencyConsumptionLimitationConfiguration extends AbstractActarisObject {

    public EmergencyConsumptionLimitationConfiguration(ObjectFactory of) {
        super(of);
    }

    @Override
    protected void parse(Element element) {
        //Only ack or nack is sent back
    }

    private int ovlRate;
    private int duration;
    private int threshold;
    private int unit;

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setOvlRate(int ovlRate) {
        this.ovlRate = ovlRate;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public void setUnit(int unit) {
        this.unit = unit;
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
        Element consumptionLimitElement = doc.createElement(XMLTags.EM_CLM);
        cf.appendChild(consumptionLimitElement);

        Element numberOfIntervalsElement = doc.createElement(XMLTags.EM_CLM_DURATION);
        numberOfIntervalsElement.setTextContent(Integer.toString(duration, 16));
        consumptionLimitElement.appendChild(numberOfIntervalsElement);

        Element el = doc.createElement(XMLTags.EM_CLM_THRESHOLD);
        el.setTextContent(calcThreshold());
        consumptionLimitElement.appendChild(el);

        el = doc.createElement(XMLTags.CONSRATE);
        el.setTextContent(Integer.toString(ovlRate));
        consumptionLimitElement.appendChild(el);

        String msg = convertDocumentToString(doc);
        return (msg.substring(msg.indexOf("?>") + 2));
    }

    private String pad(String text, int length) {
        while (text.length() < length) {
            text = "0" + text;
        }
        return text;
    }

    private String calcThreshold() {
        String thresholdString = pad(Integer.toString(threshold, 16), 8);
        int first = Integer.parseInt(thresholdString.substring(0, 2), 16) & 0xFF;
        first = (first | (0x80 * unit)) & 0xFF;   //Set bit indicating the unit
        return pad(Integer.toString(first, 16)) + thresholdString.substring(2, 8);
    }

    private String pad(String text) {
        if (text.length() == 1) {
            text = "0" + text;
        }
        return text;
    }
}