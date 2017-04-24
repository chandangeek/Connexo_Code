/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.ace4000.objects;

import com.energyict.protocolimplv2.ace4000.ACE4000;
import com.energyict.protocolimplv2.ace4000.ACE4000Outbound;
import org.w3c.dom.Element;

/**
 * @author gna
 */
public class DateTime extends AbstractActarisObject {

    private long meterTime = 0;
    private long receiveTime = 0;

    public DateTime(ObjectFactory of) {
        super(of);
    }

    protected String prepareXML() {
        return "";      //Not possible to request the time :(
    }

    /**
     * Sync or force the time after receiving it;
     */
    protected void parse(Element mdElement) {
        setMeterTime(Long.valueOf(mdElement.getTextContent(), 16));                     //Value in the <DT> tags is the meter time.
        setReceiveTime(getObjectFactory().getCurrentMeterTime().getTime());             //Current time, in the meter's time zone!
        if (!getObjectFactory().isInbound()) {
            getObjectFactory().sendSyncTime(getMeterTime(), getReceiveTime());          //Have to send this as an ACK
        }
        getObjectFactory().setClockWasSet(true);
    }

    protected long getMeterTime() {
        return meterTime;
    }

    protected void setMeterTime(long meterTime) {
        this.meterTime = meterTime;
        ACE4000 ace4000 = getObjectFactory().getAce4000();
        if (ace4000 instanceof ACE4000Outbound) {
            //Cache the time difference
            ((ACE4000Outbound) ace4000).setCachedMeterTimeDifference(System.currentTimeMillis() - (meterTime * 1000));
        }
    }

    protected long getReceiveTime() {
        return receiveTime;
    }

    protected void setReceiveTime(long receiveTime) {
        this.receiveTime = receiveTime;
    }
}