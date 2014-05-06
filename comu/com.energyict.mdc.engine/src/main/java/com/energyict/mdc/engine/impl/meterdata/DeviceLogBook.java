package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.comserver.commands.CollectedLogBookDeviceCommand;
import com.energyict.comserver.commands.DeviceCommand;
import com.energyict.comserver.exceptions.CodingException;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;

import java.util.Collections;
import java.util.List;

/**
 * Implementation of a LogBook, collected from the Device.
 * If no data could be collected, then a proper {@link com.energyict.mdc.issues.Issue}
 * and {@link com.energyict.mdc.protocol.api.device.data.ResultType} will be returned.
 *
 * @author gna
 * @since 4/04/12 - 8:27
 */
public class DeviceLogBook extends CollectedDeviceData implements CollectedLogBook {

    private LogBookIdentifier logBookIdentifier;

    private List<MeterProtocolEvent> meterEvents;

    public DeviceLogBook(final LogBookIdentifier logBookIdentifier) {
        super();
        this.logBookIdentifier = logBookIdentifier;
    }

    @Override
    public boolean isConfiguredIn (DataCollectionConfiguration configuration) {
        return configuration.isConfiguredToCollectEvents();
    }

    @Override
    public DeviceCommand toDeviceCommand(IssueService issueService) {
        return new CollectedLogBookDeviceCommand(this, issueService);
    }

    @Override
    public List<MeterProtocolEvent> getCollectedMeterEvents() {
        if (this.meterEvents == null) {
            return Collections.emptyList();
        }
        return this.meterEvents;
    }

    @Override
    public LogBookIdentifier getLogBookIdentifier() {
        return logBookIdentifier;
    }

    @Override
    public void setMeterEvents(List<MeterProtocolEvent> meterEvents) {
        if(meterEvents == null){
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "setMeterEvents", "meterEvents");
        }
        this.meterEvents = meterEvents;
    }
}
