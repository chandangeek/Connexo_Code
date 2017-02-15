/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex;

import com.elster.jupiter.calendar.CalendarService;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.legacy.BulkRegisterProtocol;
import com.energyict.protocols.mdc.services.impl.OrmClient;

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

import javax.inject.Inject;
import java.io.IOException;
import java.time.Clock;

/**
 * @author sva
 * @since 29/01/13 - 14:52
 */
public class REMIDatalogger extends E350 {

    @Override
    public String getProtocolDescription() {
        return "XEMEX ReMI Datalogger DLMS";
    }

    private XemexLoadProfileBuilder loadProfileBuilder;

    @Inject
    public REMIDatalogger(PropertySpecService propertySpecService, Clock clock, TopologyService topologyService, CalendarService calendarService, OrmClient ormClient, MdcReadingTypeUtilService readingTypeUtilService, LoadProfileFactory loadProfileFactory) {
        super(propertySpecService, clock, topologyService, calendarService, ormClient, readingTypeUtilService, loadProfileFactory);
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
            this.loadProfileBuilder = new XemexLoadProfileBuilder(this, this.getReadingTypeUtilService());
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
        return new XemexMessaging(new XemexMessageExecutor(this, this.getClock(), this.getTopologyService(), this.getCalendarService()));
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
            this.meterTopology = new XemexMeterTopology(this, this.getTopologyService());
        }
        return meterTopology;
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }
}