package com.energyict.genericprotocolimpl.ace4000.objects;

import com.energyict.mdw.core.CommunicationScheduler;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.logging.Level;

/**
 * @author gna
 */
public class DateTime extends AbstractActarisObject {

    private long meterTime;
    private long receiveTime;
    private long minDiff = 0;          //Default
    private long maxDiff = 3600;       //Default

    public DateTime(ObjectFactory of) {
        super(of);
        if (getObjectFactory().getAce4000().getCommSchedulers().size() != 0) {
            for (CommunicationScheduler scheduler : getObjectFactory().getAce4000().getCommSchedulers()) {
                if (scheduler.getCommunicationProfile().getForceClock()) {
                    setMaxDiff(((long) scheduler.getCommunicationProfile().getMaximumClockDifference()) * 1000);
                    setMinDiff(((long) scheduler.getCommunicationProfile().getMinimumClockDifference()) * 1000);
                    break;
                }
            }
        } else {
            getObjectFactory().getAce4000().getLogger().log(Level.INFO, "No communicationProfile was configured on the meter, cannot set the meter clock.");
        }
    }

    protected String prepareXML() {
        return "";      //Not possible to request the time :(
    }

    /**
     * Sync or force the time after receiving it;
     */
    protected void parse(Element mdElement) throws IOException {
        //setMeterTime(Long.valueOf(mdElement.getTextContent(), 16));                //Value in the <DT> tags is the meter time.

        setMeterTime(getObjectFactory().getCurrentMeterTime().getTime());            //TODO this is a temporary bypass for the broken <DT> tag!!

        setReceiveTime(getObjectFactory().getCurrentMeterTime().getTime());          //Current time, in the meter's time zone!
        long diff = Math.abs(getMeterTime() - getReceiveTime());
        if (diff > getMaxDiff()) {
            getObjectFactory().sendForceTime();
        } else {
            getObjectFactory().sendSyncTime(getMeterTime(), getReceiveTime());         //Have to send this as an ACK
        }
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

    private void setMinDiff(long min) {
        this.minDiff = min;
    }

    private void setMaxDiff(long max) {
        this.maxDiff = max;
    }

    private long getMinDiff() {
        return minDiff;
    }

    private long getMaxDiff() {
        return maxDiff;
    }
}