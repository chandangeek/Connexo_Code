package com.energyict.protocolimplv2.nta.dsmr23.eict;

import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.protocolimplv2.nta.abstractnta.AbstractNtaMbusDevice;
import com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23MbusMessaging;

import java.util.Collections;
import java.util.List;

/**
 * @author sva
 * @since 29/11/13 - 9:59
 */
public class MbusDevice extends AbstractNtaMbusDevice {

    private Dsmr23MbusMessaging dsmr23MbusMessaging;

    protected MbusDevice(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, Extractor extractor) {
        super(propertySpecService, nlsService, converter, collectedDataFactory, issueFactory, extractor);
    }

    @Override
    public DeviceMessageSupport getDeviceMessageSupport() {
        if (dsmr23MbusMessaging == null) {
            dsmr23MbusMessaging = new Dsmr23MbusMessaging(this, this.getExtractor(), this.getPropertySpecService(), this.getNlsService(), this.getConverter());
        }
        return dsmr23MbusMessaging;
    }

    @Override
    public String getProtocolDescription() {
        return "EnergyICT WebRTU KP DLMS (NTA DSMR2.3) Mbus Slave V2";
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-03-30 15:24:34 +0200 (Mon, 30 Mar 2015) $";
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return DeviceFunction.NONE;
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;
    }

    @Override
    public CollectedCalendar getCollectedCalendar() {
        return null;
    }

    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        return null;
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        return null;
    }
}