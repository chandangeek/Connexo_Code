package com.energyict.protocolimplv2.eict.rtu3.beacon3100.logbooks;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.protocol.MeterEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by iulian on 7/26/2016.
 */
public abstract class Beacon3100AbstractEventLog {
    protected TimeZone timeZone;
    protected DataContainer dcEvents;
    protected List<MeterEvent> meterEvents;

    public Beacon3100AbstractEventLog(DataContainer dc, TimeZone timeZone) {
        this.timeZone = timeZone;
        this.dcEvents = dc;
    }

    protected abstract String getLogBookName();


    protected abstract void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int dlmsCode, int deviceCode, String message);


    /** https://confluence.eict.vpdc/display/G3IntBeacon3100/DLMS+event+log+books
     *
     * Event structure details:
     *
     * 0:   Timestamp        COSEM-DATE-TIME     Event timestamp
     * 1:   DLMS code        long-unsigned       Event DLMS code
     * 2:   Device code      long-unsigned       Event device code
     * 3:   Message          OCTET-STRING        (optional) message
     *

     * @return the MeterEvent List
     */
    public List<MeterEvent> getMeterEvents() {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        int size = this.dcEvents.getRoot().getNrOfElements();
        Date eventTimeStamp;
        long dlmsCode;
        long deviceCode;
        String  message;

        for (int i = 0; i <= (size - 1); i++) {
            DataStructure eventStructure = this.dcEvents.getRoot().getStructure(i);

            eventTimeStamp = eventStructure.getOctetString(0).toDate(timeZone);
            dlmsCode = (long) eventStructure.getValue(1) & 0xFFFFFFF;      // To prevent negative values
            deviceCode = (long) eventStructure.getValue(2) & 0xFFFFFFF;    // To prevent negative values
            message = eventStructure.getOctetString(3).toString();

            buildMeterEvent(meterEvents, eventTimeStamp, (int)dlmsCode, (int)deviceCode, message);
        }

        return meterEvents;
    }

    protected String getDefaultEventDescription(int dlmsCode, int deviceCode, String message){
        return "Unknown eventcode: dlmsCode=" + dlmsCode + ", deviceCode="+deviceCode+", message=["+message+"] in "+getLogBookName()+"";
    }


    /**
     * Checks if the given {@link Object} is an {@link com.energyict.dlms.OctetString}
     *
     * @param element the object to check the type
     * @return true or false
     */
    protected boolean isOctetString(Object element) {
        return (element instanceof com.energyict.dlms.OctetString);
    }
}
