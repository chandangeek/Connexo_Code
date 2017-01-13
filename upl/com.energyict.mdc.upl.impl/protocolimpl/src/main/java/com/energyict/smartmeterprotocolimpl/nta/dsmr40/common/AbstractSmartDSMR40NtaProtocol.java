package com.energyict.smartmeterprotocolimpl.nta.dsmr40.common;

import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocol.BulkRegisterProtocol;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.EventProfile;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.DSMR40RegisterFactory;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.Dsmr40Properties;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.eventhandling.DSMR40EventProfile;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40MessageExecutor;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40Messaging;

/**
 * Abstract class to group functionality for all <b>DSMR4.0</b> protocols
 */
public abstract class AbstractSmartDSMR40NtaProtocol extends AbstractSmartNtaProtocol {

    /**
     * The used {@link com.energyict.smartmeterprotocolimpl.nta.dsmr40.eventhandling.DSMR40EventProfile}
     */
    protected DSMR40EventProfile eventProfile;
    private final TariffCalendarFinder calendarFinder;
    private final TariffCalendarExtractor calendarExtractor;
    private final DeviceMessageFileFinder messageFileFinder;
    private final DeviceMessageFileExtractor messageFileExtractor;

    protected AbstractSmartDSMR40NtaProtocol(PropertySpecService propertySpecService, TariffCalendarFinder calendarFinder, TariffCalendarExtractor calendarExtractor, DeviceMessageFileFinder messageFileFinder, DeviceMessageFileExtractor messageFileExtractor) {
        super(propertySpecService);
        this.calendarFinder = calendarFinder;
        this.calendarExtractor = calendarExtractor;
        this.messageFileFinder = messageFileFinder;
        this.messageFileExtractor = messageFileExtractor;
    }

    public TariffCalendarFinder getCalendarFinder() {
        return calendarFinder;
    }

    public TariffCalendarExtractor getCalendarExtractor() {
        return calendarExtractor;
    }

    public DeviceMessageFileFinder getMessageFileFinder() {
        return messageFileFinder;
    }

    public DeviceMessageFileExtractor getMessageFileExtractor() {
        return messageFileExtractor;
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        return new Dsmr40Messaging(new Dsmr40MessageExecutor(this, this.calendarFinder, this.calendarExtractor, messageFileFinder, this.messageFileExtractor));
    }

    /**
     * Getter for the {@link com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties}
     *
     * @return the requested Properties
     */
    @Override
    public DlmsProtocolProperties getProperties() {
        if (this.properties == null) {
            this.properties = new Dsmr40Properties(this.getPropertySpecService());
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
