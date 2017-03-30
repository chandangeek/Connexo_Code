/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.engine.impl.commands.store.CreateNoLogBooksForDeviceEvent;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.protocol.api.device.data.NoLogBooksCollectedData;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;
import com.energyict.protocol.MeterProtocolEvent;

import javax.xml.bind.annotation.XmlElement;
import java.util.Collections;
import java.util.List;

/**
 * @author sva
 * @since 14/12/12 - 9:06
 */

public class NoLogBooksForDevice extends CollectedDeviceData implements NoLogBooksCollectedData {

    private final DeviceIdentifier deviceIdentifier;

    public NoLogBooksForDevice(DeviceIdentifier deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public DeviceCommand toDeviceCommand(MeterDataStoreCommand meterDataStoreCommand, DeviceCommand.ServiceProvider serviceProvider) {
        return new CreateNoLogBooksForDeviceEvent(this, this.getComTaskExecution(), serviceProvider);
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration configuration) {
        return false;
    }

    @Override
    public List<MeterProtocolEvent> getCollectedMeterEvents() {
        return Collections.emptyList();
    }

    @Override
    public LogBookIdentifier getLogBookIdentifier() {
        throw new IllegalAccessError("The NoLogBook object does not support a LogBook");
    }

    @Override
    public void setCollectedMeterEvents(List<MeterProtocolEvent> meterEvents) {
        // nothing to do
    }

    @Override
    public void addCollectedMeterEvents(List<MeterProtocolEvent> meterEvents) {
        // nothing to do
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getSimpleName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }
}
