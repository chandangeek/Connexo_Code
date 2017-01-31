/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.protocols.mdc.services.impl.OrmClient;
import com.energyict.protocols.messaging.LegacyLoadProfileRegisterMessageBuilder;
import com.energyict.protocols.messaging.LegacyPartialLoadProfileMessageBuilder;

import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractNtaMbusDevice;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.messages.XemexMbusMessaging;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Collections;
import java.util.List;

/**
 * @author sva
 * @since 30/01/13 - 10:18
 */
public class MbusDevice extends AbstractNtaMbusDevice {

    @Override
    public String getProtocolDescription() {
        return "XEMEX ReMI Datalogger DLMS Mbus Slave";
    }

    @Inject
    public MbusDevice(PropertySpecService propertySpecService, Clock clock, TopologyService topologyService, CalendarService calendarService, MdcReadingTypeUtilService readingTypeUtilService, LoadProfileFactory loadProfileFactory, OrmClient ormClient) {
        super(clock, topologyService, calendarService, readingTypeUtilService, loadProfileFactory, ormClient, propertySpecService);
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        return new XemexMbusMessaging();
    }

    /**
     * Returns the implementation version
     *
     * @return a version string
     */
    public String getVersion() {
        return "$Date: 2012-08-06 14:46:33 +0200 (ma, 06 aug 2012) $";
    }

    @Override
    public void addProperties(TypedProperties properties) {
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Collections.emptyList();
    }

    public LegacyLoadProfileRegisterMessageBuilder getLoadProfileRegisterMessageBuilder() {
        return new LegacyLoadProfileRegisterMessageBuilder(this.getClock(), this.getTopologyService(), this.getLoadProfileFactory());
    }

    public LegacyPartialLoadProfileMessageBuilder getPartialLoadProfileMessageBuilder() {
        return new LegacyPartialLoadProfileMessageBuilder(this.getClock(), this.getTopologyService(), this.getLoadProfileFactory());
    }

}