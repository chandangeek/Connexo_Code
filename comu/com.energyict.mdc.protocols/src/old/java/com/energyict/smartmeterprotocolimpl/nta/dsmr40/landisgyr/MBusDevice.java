package com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.protocols.messaging.LegacyLoadProfileRegisterMessageBuilder;
import com.energyict.protocols.messaging.LegacyPartialLoadProfileMessageBuilder;

import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractNtaMbusDevice;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MbusMessaging;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Collections;
import java.util.List;

/**
 * Place holder class for the MBus device.
 * Contains the standard DSMR 2.3 MBus messages (connect control and setup)
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 16:04
 * Author: khe
 */
public class MBusDevice extends AbstractNtaMbusDevice {

    @Override
    public String getProtocolDescription() {
        return "Landis+Gyr E350 XEMEX DLMS (NTA DSMR4.0) Mbus Slave";
    }

    @Inject
    public MBusDevice(PropertySpecService propertySpecService, Clock clock, TopologyService topologyService, CalendarService calendarService, MdcReadingTypeUtilService readingTypeUtilService, LoadProfileFactory loadProfileFactory, OrmClient ormClient) {
        super(clock, topologyService, calendarService, readingTypeUtilService, loadProfileFactory, ormClient, propertySpecService);
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        return new Dsmr23MbusMessaging();
    }

    /**
     * Returns the implementation version
     *
     * @return a version string
     */
    public String getVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

    @Override
    public void addProperties(TypedProperties properties) {
    }

    public LegacyLoadProfileRegisterMessageBuilder getLoadProfileRegisterMessageBuilder() {
        return new LegacyLoadProfileRegisterMessageBuilder(this.getClock(), this.getTopologyService(), this.getLoadProfileFactory());
    }

    public LegacyPartialLoadProfileMessageBuilder getPartialLoadProfileMessageBuilder() {
        return new LegacyPartialLoadProfileMessageBuilder(this.getClock(), this.getTopologyService(), this.getLoadProfileFactory());
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Collections.emptyList();
    }

}