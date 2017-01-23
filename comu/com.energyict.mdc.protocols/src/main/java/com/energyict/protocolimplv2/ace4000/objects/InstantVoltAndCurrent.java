package com.energyict.protocolimplv2.ace4000.objects;

import com.energyict.mdc.protocol.api.device.data.MeterReadingData;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.ace4000.xml.XMLTags;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Date;

/**
 * @author khe
 */
public class InstantVoltAndCurrent extends AbstractActarisObject {

    private MeterReadingData mrd = new MeterReadingData();
    private Date timeStamp = null;

    public InstantVoltAndCurrent(ObjectFactory of) {
        super(of);
    }

    protected void parse(Element mdElement) {
        NodeList list = mdElement.getChildNodes();

        for (int i = 0; i < list.getLength(); i++) {
            Element element = (Element) list.item(i);

            if (element.getNodeName().equalsIgnoreCase(XMLTags.INSTTIME)) {
                timeStamp = getObjectFactory().convertMeterDateToSystemDate(Long.valueOf(mdElement.getChildNodes().item(0).getTextContent(), 16));
            } else if (element.getNodeName().equalsIgnoreCase(XMLTags.PHASE)) {
                parseRegisters(element);
            }
        }
    }

    private void parseRegisters(Element element) {
        int phase = Integer.parseInt(element.getAttribute(XMLTags.PHASEATTR));
        if (phase == 1 || phase == 2 | phase == 3) {
            NodeList list = element.getChildNodes();

            for (int i = 0; i < list.getLength(); i++) {
                Element registerElement = (Element) list.item(i);
                Quantity quantity;

                if (registerElement.getNodeName().equalsIgnoreCase(XMLTags.VOLTAGE)) {
                    Long value = Long.valueOf(registerElement.getTextContent(), 16);
                    int dField = 32 + (phase - 1) * 20;
                    ObisCode obisCode = ObisCode.fromString("1.0." + dField + ".7.0.255");
                    quantity = new Quantity(value, Unit.get(BaseUnit.VOLT, -2));
                    mrd.add(new RegisterValue(obisCode, quantity, new Date(), getTimeStamp()));
                } else if (registerElement.getNodeName().equalsIgnoreCase(XMLTags.CURRENT)) {
                    Long value = Long.valueOf(registerElement.getTextContent(), 16);
                    int dField = 31 + (phase - 1) * 20;
                    ObisCode obisCode = ObisCode.fromString("1.0." + dField + ".7.0.255");
                    quantity = new Quantity(value, Unit.get(BaseUnit.AMPERE, -2));
                    mrd.add(new RegisterValue(obisCode, quantity, new Date(), getTimeStamp()));
                } else if (registerElement.getNodeName().equalsIgnoreCase(XMLTags.ACTPOW)) {
                    Long value = Long.valueOf(registerElement.getTextContent(), 16);
                    int dField = 20 * phase + 1;
                    ObisCode obisCode = ObisCode.fromString("1.0." + dField + ".7.0.255");
                    quantity = new Quantity(value, Unit.get(BaseUnit.WATT));
                    mrd.add(new RegisterValue(obisCode, quantity, new Date(), getTimeStamp()));
                } else if (registerElement.getNodeName().equalsIgnoreCase(XMLTags.REACTPOW)) {
                    Long value = Long.valueOf(registerElement.getTextContent(), 16);
                    int dField = 20 * phase + 3;
                    ObisCode obisCode = ObisCode.fromString("1.0." + dField + ".7.0.255");
                    quantity = new Quantity(value, Unit.get(BaseUnit.VOLTAMPEREREACTIVE));
                    mrd.add(new RegisterValue(obisCode, quantity, new Date(), getTimeStamp()));
                } else if (registerElement.getNodeName().equalsIgnoreCase(XMLTags.APPARPOW)) {
                    Long value = Long.valueOf(registerElement.getTextContent(), 16);
                    int dField = 20 * phase + 9;
                    ObisCode obisCode = ObisCode.fromString("1.0." + dField + ".7.0.255");
                    quantity = new Quantity(value, Unit.get(BaseUnit.VOLTAMPERE));
                    mrd.add(new RegisterValue(obisCode, quantity, new Date(), getTimeStamp()));
                } else if (registerElement.getNodeName().equalsIgnoreCase(XMLTags.POWERFACTOR)) {
                    Long value = Long.valueOf(registerElement.getTextContent(), 16);
                    int dField = 84 + phase;
                    ObisCode obisCode = ObisCode.fromString("1.0." + dField + ".7.0.255");
                    quantity = new Quantity(value, Unit.get(""));
                    mrd.add(new RegisterValue(obisCode, quantity, new Date(), getTimeStamp()));
                }
            }
        }
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
        Element lp = doc.createElement(XMLTags.REQINSTVC);
        md.appendChild(lp);

        String msg = convertDocumentToString(doc);
        return (msg.substring(msg.indexOf("?>") + 2));
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    protected void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    protected MeterReadingData getMrd() {
        return mrd;
    }
}