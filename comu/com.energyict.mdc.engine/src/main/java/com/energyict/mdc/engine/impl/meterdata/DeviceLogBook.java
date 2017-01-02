package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.store.CollectedLogBookDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;
import com.energyict.protocol.MeterProtocolEvent;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of a LogBook, collected from the Device.
 * If no data could be collected, then a proper {@link com.energyict.mdc.upl.issue.Issue}
 * and {@link com.energyict.mdc.upl.meterdata.ResultType} will be returned.
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
    public DeviceCommand toDeviceCommand(MeterDataStoreCommand meterDataStoreCommand, DeviceCommand.ServiceProvider serviceProvider) {
        return new CollectedLogBookDeviceCommand(this, this.getComTaskExecution(), meterDataStoreCommand);
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
    public void setCollectedMeterEvents(List<MeterProtocolEvent> meterEvents) {
        if (meterEvents == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "setCollectedMeterEvents", "meterEvents", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        this.meterEvents = meterEvents;
    }

    @Override
    public void addCollectedMeterEvents(List<MeterProtocolEvent> meterEvents) {
        if (meterEvents == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "addCollectedMeterEvents", "meterEvents", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        if (this.meterEvents == null) {
            this.meterEvents = new ArrayList<>();
        }
        this.meterEvents = meterEvents;
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getSimpleName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }
}
