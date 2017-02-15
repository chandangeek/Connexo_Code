/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr40.common;

import com.elster.jupiter.calendar.CalendarService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.legacy.BulkRegisterProtocol;
import com.energyict.protocols.mdc.services.impl.OrmClient;

import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.EventProfile;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.DSMR40RegisterFactory;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.Dsmr40Properties;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.eventhandling.DSMR40EventProfile;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40MessageExecutor;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40Messaging;

import java.time.Clock;

/**
 * Abstract class to group functionality for all <b>DSMR4.0</b> protocols
 */
public abstract class AbstractSmartDSMR40NtaProtocol extends AbstractSmartNtaProtocol {

    private final CalendarService calendarService;

    /**
     * The used {@link com.energyict.smartmeterprotocolimpl.nta.dsmr40.eventhandling.DSMR40EventProfile}
     */
    protected DSMR40EventProfile eventProfile;

    public AbstractSmartDSMR40NtaProtocol(PropertySpecService propertySpecService, Clock clock, TopologyService topologyService, CalendarService calendarService, OrmClient ormClient, MdcReadingTypeUtilService readingTypeUtilService, LoadProfileFactory loadProfileFactory) {
        super(propertySpecService, clock, topologyService, readingTypeUtilService, loadProfileFactory, ormClient);
        this.calendarService = calendarService;
    }

    protected CalendarService getCalendarService() {
        return calendarService;
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        return new Dsmr40Messaging(new Dsmr40MessageExecutor(this, this.getClock(), this.getTopologyService(), this.calendarService));
    }

    /**
     * Getter for the {@link com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties}
     *
     * @return the requested Properties
     */
    @Override
    public DlmsProtocolProperties getProperties() {
        if (this.properties == null) {
            this.properties = new Dsmr40Properties();
        }
        return this.properties;
    }

    @Override
    public BulkRegisterProtocol getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new DSMR40RegisterFactory(this);
        }
        return this.registerFactory;
    }

    /**
     * Getter for the <b>DSMR 4.0</b> DSMR40EventProfile
     *
     * @return the lazy loaded EventProfile
     */
    @Override
    public EventProfile getEventProfile() {
        if (this.eventProfile == null) {
            this.eventProfile = new DSMR40EventProfile(this);
        }
        return this.eventProfile;
    }
}
