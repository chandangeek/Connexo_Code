package com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E35C;

import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.composedobjects.ComposedMeterInfo;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.EventProfile;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.LoadProfileBuilder;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.topology.MeterTopology;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E350;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E35C.events.E35CEventProfile;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E35C.profiles.E35CLoadProfileBuilder;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;

/**
 * The E35C is an Ethernet communication module for the E350 electricity meter<br/>
 * The protocol extends from the regular E350 protocol, which was developed for communication using GPRS module.
 *
 * @author sva
 * @since 22/06/2015 - 14:09
 */
public class E35C extends E350 {

    private E35CMeterInfo meterInfo;
    protected E35CProperties properties;
    private E35CMeterTopology meterTopology;

    public ComposedMeterInfo getMeterInfo() {
        if (meterInfo == null) {
            meterInfo = new E35CMeterInfo(getDlmsSession(), supportsBulkRequests());
        }
        return meterInfo;
    }

    @Override
    public MeterTopology getMeterTopology() {
        if (this.meterTopology == null) {
            this.meterTopology = new E35CMeterTopology(this);
        }
        return meterTopology;
    }

    @Override
    public E35CProperties getProperties() {
        if (this.properties == null) {
            this.properties = new E35CProperties();
        }
        return this.properties;
    }

    @Override
    public LoadProfileBuilder getLoadProfileBuilder() {
        if (this.loadProfileBuilder == null) {
            this.loadProfileBuilder = new E35CLoadProfileBuilder(this);
        }
        return loadProfileBuilder;
    }

    @Override
    public EventProfile getEventProfile() {
        if (this.eventProfile == null) {
            this.eventProfile = new E35CEventProfile(this);
        }
        return this.eventProfile;
    }

    @Override
    public void setTime(Date newMeterTime) throws IOException {
        try {
            AXDRDateTime dateTime = new AXDRDateTime(newMeterTime, getTimeZone());
            dateTime.useUnspecifiedAsDeviation(true);
            this.dlmsSession.getCosemObjectFactory().getClock().setAXDRDateTimeAttr(dateTime);
        } catch (IOException e) {
            getLogger().log(Level.FINEST, e.getMessage());
            throw new IOException("Could not set the Clock object." + e);
        }
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }
}