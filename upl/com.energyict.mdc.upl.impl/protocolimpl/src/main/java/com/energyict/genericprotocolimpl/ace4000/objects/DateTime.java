package com.energyict.genericprotocolimpl.ace4000.objects;

import org.w3c.dom.Element;

import java.io.IOException;

/**
 * @author gna
 */
public class DateTime extends AbstractActarisObject {

    private long meterTime;
    private long receiveTime;

    public DateTime(ObjectFactory of) {
        super(of);
    }

    protected String prepareXML() {
        return "";      //Not possible to request the time :(
    }

    /**
     * Sync or force the time after receiving it;
     */
    protected void parse(Element mdElement) throws IOException {
        setMeterTime(Long.valueOf(mdElement.getTextContent(), 16));                     //Value in the <DT> tags is the meter time.
        setReceiveTime(getObjectFactory().getCurrentMeterTime().getTime());             //Current time, in the meter's time zone!
        getObjectFactory().sendSyncTime(getMeterTime(), getReceiveTime());              //Have to send this as an ACK
        getObjectFactory().setClockWasSet(true);
    }

    protected long getMeterTime() {
        return meterTime;
    }

    protected void setMeterTime(long meterTime) {
        this.meterTime = meterTime;
    }

    protected long getReceiveTime() {
        return receiveTime;
    }

    protected void setReceiveTime(long receiveTime) {
        this.receiveTime = receiveTime;
    }
}