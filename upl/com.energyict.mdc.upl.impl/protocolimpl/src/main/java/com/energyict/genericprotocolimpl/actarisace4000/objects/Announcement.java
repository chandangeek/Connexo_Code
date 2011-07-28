package com.energyict.genericprotocolimpl.actarisace4000.objects;

import com.energyict.genericprotocolimpl.actarisace4000.objects.tables.MeterTypeTable;
import com.energyict.genericprotocolimpl.actarisace4000.objects.xml.XMLTags;
import com.energyict.protocol.MeterEvent;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Date;
import java.util.logging.Level;

/**
 * @author gna
 */
public class Announcement extends AbstractActarisObject {

    private String ICID = null;        // SIM card ICID number
    private String type = null;        // meter type code
    private int sStrength = 0;        // GSM signal strength
    private int bStationID = 0;        // GSM cell base station ID
    private String opName = null;        // GSM opertor name
    private String cString = null;        // meter codification string

    public Announcement(ObjectFactory of) {
        super(of);
    }

    protected void parse(Element mdElement) {
        NodeList list = mdElement.getElementsByTagName(XMLTags.ANNOUNCE);

        for (int i = 0; i < list.getLength(); i++) {
            Element element = (Element) list.item(i);

            if (element.getNodeName().equalsIgnoreCase(XMLTags.LOST)) {
                String msg;
                if (list.getLength() == 1) {
                    msg = "Communication with slave [" + getSerialNumber() + "] was lost";
                    getObjectFactory().log(Level.INFO, msg);
                } else {
                    msg = "Meter [" + getSerialNumber() + "] was removed.";
                    getObjectFactory().log(Level.INFO, msg);
                }
                getObjectFactory().getMeterEvents().add(new MeterEvent(new Date(), MeterEvent.OTHER, 0, msg));
            } else if (element.getNodeName().equalsIgnoreCase(XMLTags.ICID)) {
                setICID(element.getTextContent());
            } else if (element.getNodeName().equalsIgnoreCase(XMLTags.TYPE)) {
                setType(MeterTypeTable.meterType[Integer.parseInt(element.getTextContent())]);
            } else if (element.getNodeName().equalsIgnoreCase(XMLTags.SSTRENGTH)) {
                setSStrength(Integer.parseInt(element.getTextContent()));
            } else if (element.getNodeName().equalsIgnoreCase(XMLTags.BSTATION)) {
                setBStationID(Integer.parseInt(element.getTextContent()));
            } else if (element.getNodeName().equalsIgnoreCase(XMLTags.OPERATORNAME)) {
                setOpName(element.getTextContent());
            } else if (element.getNodeName().equalsIgnoreCase(XMLTags.CODSTRING)) {
                setCString(element.getTextContent());
            } else if (element.getNodeName().equalsIgnoreCase(XMLTags.CURREADING)) {
                getObjectFactory().getCurrentReadings().parse(element);
            }

        }
    }

    @Override
    protected String prepareXML() {
        return "";      //An announcement is never sent from the system to the meter.
    }

    protected String getICID() {
        return ICID;
    }

    protected void setICID(String icid) {
        ICID = icid;
    }

    protected String getType() {
        return type;
    }

    protected void setType(String type) {
        this.type = type;
    }

    protected int getSStrength() {
        return sStrength;
    }

    protected void setSStrength(int strength) {
        sStrength = strength;
    }

    protected int getBStationID() {
        return bStationID;
    }

    protected void setBStationID(int stationID) {
        bStationID = stationID;
    }

    protected String getOpName() {
        return opName;
    }

    protected void setOpName(String opName) {
        this.opName = opName;
    }

    protected String getCString() {
        return cString;
    }

    protected void setCString(String string) {
        cString = string;
    }
}