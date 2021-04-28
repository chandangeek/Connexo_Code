/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.store.CollectedLogBookDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.identifiers.LogBookIdentifierByDeviceAndObisCode;
import com.energyict.mdc.identifiers.LogBookIdentifierById;
import com.energyict.mdc.identifiers.LogBookIdentifierByObisCodeAndDevice;
import com.energyict.mdc.identifiers.LogBookIdentifierForAlreadyKnowLogBook;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;
import com.energyict.protocol.MeterProtocolEvent;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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

    private boolean awareOfPushedEvents;

    private LogBookIdentifier logBookIdentifier;

    private List<MeterProtocolEvent> meterEvents;

    public DeviceLogBook() {
        super();
    }

    public DeviceLogBook(final LogBookIdentifier logBookIdentifier) {
        super();
        this.logBookIdentifier = logBookIdentifier;
        this.awareOfPushedEvents = false;
    }

    public DeviceLogBook(final LogBookIdentifier logBookIdentifier, boolean awareOfPushedEvents) {
        super();
        this.logBookIdentifier = logBookIdentifier;
        this.awareOfPushedEvents = awareOfPushedEvents;
    }

    @Override
    public boolean isConfiguredIn (DataCollectionConfiguration configuration) {
        return configuration.isConfiguredToCollectEvents();
    }

    @XmlAttribute
    public boolean isAwareOfPushedEvents() {
        return awareOfPushedEvents;
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
        return getCollectedMeterEventsWithoutFutureEvents();
    }

    private List<MeterProtocolEvent> getCollectedMeterEventsWithoutFutureEvents() {
        List<MeterProtocolEvent> filteredEvents = new ArrayList<>();
        for (MeterProtocolEvent meterEvent : this.meterEvents) {
            if (!meterEvent.getTime().after(new Date())) {
                filteredEvents.add(meterEvent);
            }
        }
        return filteredEvents;
    }

    @XmlElements( {
            @XmlElement(type = LogBookIdentifierById.class),
            @XmlElement(type = LogBookIdentifierByObisCodeAndDevice.class),
            @XmlElement(type = LogBookIdentifierByDeviceAndObisCode.class),
            @XmlElement(type = LogBookIdentifierForAlreadyKnowLogBook.class),
    })
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
}
