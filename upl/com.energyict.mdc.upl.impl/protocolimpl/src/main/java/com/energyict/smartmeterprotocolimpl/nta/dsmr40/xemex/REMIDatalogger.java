package com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex;

import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.BulkRegisterProtocol;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.EventProfile;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.topology.MeterTopology;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E350;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.eventhandling.XemexEventProfile;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.messages.XemexMessageExecutor;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.messages.XemexMessaging;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.profiles.XemexLoadProfileBuilder;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.topology.XemexMeterTopology;

import java.io.IOException;

/**
 * @author sva
 * @since 29/01/13 - 14:52
 */
public class REMIDatalogger extends E350 {

    private XemexLoadProfileBuilder loadProfileBuilder;

    public REMIDatalogger(TariffCalendarFinder calendarFinder, TariffCalendarExtractor calendarExtractor, DeviceMessageFileFinder messageFileFinder, DeviceMessageFileExtractor messageFileExtractor) {
        super(calendarFinder, calendarExtractor, messageFileFinder, messageFileExtractor);
    }

    @Override
    public DlmsProtocolProperties getProperties() {
        if (this.properties == null) {
            this.properties = new XemexProperties();
        }
        return this.properties;
    }

    @Override
    public XemexLoadProfileBuilder getLoadProfileBuilder() {
        if (this.loadProfileBuilder == null) {
            this.loadProfileBuilder = new XemexLoadProfileBuilder(this);
        }
        return loadProfileBuilder;
    }

    @Override
    public EventProfile getEventProfile() {
        if (this.eventProfile == null) {
            this.eventProfile = new XemexEventProfile(this);
        }
        return this.eventProfile;
    }

    /**
     * Get the SerialNumber of the device
     *
     * @return the serialNumber of the device
     * @throws java.io.IOException thrown in case of an exception
     */
    public String getMeterSerialNumber() throws IOException {
        try {
            return getMeterInfo().getSerialNr();
        } catch (IOException e) {
            String message = "Could not retrieve the serialnumber of the meter. " + e.getMessage();
            getLogger().finest(message);
            throw e;
        }
    }

    @Override
    public BulkRegisterProtocol getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new XemexRegisterFactory(this);
        }
        return this.registerFactory;
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        return new XemexMessaging(new XemexMessageExecutor(this, this.getCalendarFinder(), this.getCalendarExtractor(), this.getMessageFileFinder(), this.getMessageFileExtractor()));
    }

    @Override
    public ObisCode getPhysicalAddressCorrectedObisCode(ObisCode obisCode, String serialNumber) {
        int address;

        if (obisCode.equalsIgnoreBChannel(monthlyObisCode)) {
            address = 0;
        } else {
            address = getPhysicalAddressFromSerialNumber(serialNumber);
        }
        if ((address == 0 && obisCode.getB() != -1) || obisCode.getB() == 128) { // then don't correct the obisCode
            return obisCode;
        }
        if (address != -1) {
            return ProtocolTools.setObisCodeField(obisCode, ObisCodeBFieldIndex, (byte) address);
        }
        return null;
    }

    @Override
    public MeterTopology getMeterTopology() {
        if (this.meterTopology == null) {
            this.meterTopology = new XemexMeterTopology(this);
        }
        return meterTopology;
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }
}