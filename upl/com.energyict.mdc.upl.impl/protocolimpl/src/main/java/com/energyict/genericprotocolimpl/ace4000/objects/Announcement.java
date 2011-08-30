package com.energyict.genericprotocolimpl.ace4000.objects;

import com.energyict.genericprotocolimpl.ace4000.objects.xml.XMLTags;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Date;
import java.util.logging.Level;

/**
 * @author gna
 */
public class Announcement extends AbstractActarisObject {

    private String ICID = null;        // SIM card ICID number
    private int type = 0;            // meter type code
    private String sStrength = "";        // GSM signal strength
    private String bStationID = "";        // GSM cell base station ID
    private String opName = null;        // GSM opertor name
    private String codificationString = null;        // meter codification string
    private static String DCMETER = "1";
    private static String CTMETER = "2";

    public Announcement(ObjectFactory of) {
        super(of);
    }

    protected void parse(Element mdElement) {
        NodeList list = mdElement.getChildNodes();

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
                setType(Integer.parseInt(element.getTextContent()));
            } else if (element.getNodeName().equalsIgnoreCase(XMLTags.SSTRENGTH)) {
                setSStrength(element.getTextContent());
            } else if (element.getNodeName().equalsIgnoreCase(XMLTags.BSTATION)) {
                setBStationID(element.getTextContent());
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

    protected int getType() {
        return type;
    }

    protected void setType(int type) {
        this.type = type;
    }

    protected String getSStrength() {
        return sStrength;
    }

    protected void setSStrength(String strength) {
        sStrength = strength;
    }

    protected String getBStationID() {
        return bStationID;
    }

    protected void setBStationID(String stationID) {
        bStationID = stationID;
    }

    protected String getOpName() {
        return opName;
    }

    protected void setOpName(String opName) {
        this.opName = opName;
    }

    protected String getCString() {
        return codificationString;
    }

    protected void setCString(String string) {
        codificationString = string;
    }

    public String getMeterType() {
        byte[] codification = ProtocolTools.getBytesFromHexString(codificationString, "");
        //if (codification.length < 12) {
        //    getObjectFactory().log(Level.WARNING, "Unrecognized meter type (in codification string), taking default: DC meter");
        //    return DCMETER;
        //}
        //
        //String modelType = new String(codification).substring(0, 2);
        //int meterType = Integer.parseInt(new String(new byte[]{codification[11]}));
        //if (modelType.equals("GT")) {
        //    if (meterType == 2) {
        //        return CTMETER;
        //    }
        //    if (meterType == 3) {
        //        return DCMETER;
        //    }
        //} else if (modelType.equals("GS") || modelType.equals("GI")) {
        //    if (meterType == 0) {
        //        return CTMETER;
        //    } else {
        //        return DCMETER;
        //    }
        //}
        //getObjectFactory().log(Level.WARNING, "Unrecognized meter type (in codification string), taking default: DC meter");
        return DCMETER;
    }
}