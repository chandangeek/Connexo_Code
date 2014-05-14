package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.comserver.commands.CreateNoLogBooksForDeviceEvent;
import com.energyict.comserver.commands.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.CreateNoLogBooksForDeviceEvent;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.device.data.NoLogBooksCollectedData;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

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
    public DeviceCommand toDeviceCommand(IssueService issueService) {
        return new CreateNoLogBooksForDeviceEvent(this, issueService);
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
    public void setMeterEvents(List<MeterProtocolEvent> meterEvents) {
        // nothing to do
    }
}
