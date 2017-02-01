/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.readings.EndDeviceEvent;
import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.events.datastorage.MeterDataStorageEvent;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MeterDataStoreCommandImpl extends DeviceCommandImpl<MeterDataStorageEvent> implements MeterDataStoreCommand {

    private final Map<String, Pair<DeviceIdentifier<Device>, MeterReadingImpl>> meterReadings = new HashMap<>();
    private final Map<LoadProfileIdentifier, Instant> lastReadings = new HashMap<>();
    private final Map<LogBookIdentifier, Instant> lastLogBooks = new HashMap<>();

    public MeterDataStoreCommandImpl(ComTaskExecution comTaskExecution, DeviceCommand.ServiceProvider serviceProvider) {
        super(comTaskExecution, serviceProvider);
    }

    @Override
    protected final void doExecute(ComServerDAO comServerDAO) {
        try {
            for (Map.Entry<String, Pair<DeviceIdentifier<Device>, MeterReadingImpl>> deviceMeterReadingEntry : meterReadings.entrySet()) {
                comServerDAO.storeMeterReadings(deviceMeterReadingEntry.getValue().getFirst(), deviceMeterReadingEntry.getValue().getLast());
            }

            comServerDAO.updateLastDataSourceReadingsFor(lastReadings, lastLogBooks);
        }
        catch (RuntimeException e) {
            handleUnexpectedExecutionException(e);
        }
    }

    void handleUnexpectedExecutionException(RuntimeException e) {
        this.getExecutionLogger().logUnexpected(e, this.getComTaskExecution());
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {

    }

    @Override
    public void addIntervalReadings(DeviceIdentifier<Device> deviceIdentifier, List<IntervalBlock> intervalBlocks) {
        Pair<DeviceIdentifier<Device>, MeterReadingImpl> meterReadingsEntry = meterReadings.get(deviceIdentifier.getIdentifier());
        if (meterReadingsEntry != null) {
            meterReadingsEntry.getLast().addAllIntervalBlocks(intervalBlocks);
        } else {
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            meterReading.addAllIntervalBlocks(intervalBlocks);
            this.meterReadings.put(deviceIdentifier.getIdentifier(), Pair.of(deviceIdentifier, meterReading));
        }
    }

    @Override
    public void addLastReadingUpdater(LoadProfileIdentifier loadProfileIdentifier, Instant lastReading) {
        Instant existingLastReading = this.lastReadings.get(loadProfileIdentifier);
        if ((existingLastReading == null) || (lastReading != null && lastReading.isAfter(existingLastReading))) {
            this.lastReadings.put(loadProfileIdentifier, lastReading);
        }
    }

    @Override
    public void addReadings(DeviceIdentifier<Device> deviceIdentifier, List<Reading> registerReadings) {
        Pair<DeviceIdentifier<Device>, MeterReadingImpl> meterReadingsEntry = meterReadings.get(deviceIdentifier.getIdentifier());
        if (meterReadingsEntry != null) {
            meterReadingsEntry.getLast().addAllReadings(registerReadings);
        } else {
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            meterReading.addAllReadings(registerReadings);
            this.meterReadings.put(deviceIdentifier.getIdentifier(), Pair.of(deviceIdentifier, meterReading));
        }
    }

    @Override
    public void addEventReadings(DeviceIdentifier<Device> deviceIdentifier, List<EndDeviceEvent> endDeviceEvents) {
        Pair<DeviceIdentifier<Device>, MeterReadingImpl> meterReadingsEntry = meterReadings.get(deviceIdentifier.getIdentifier());
        if (meterReadingsEntry != null) {
            meterReadingsEntry.getLast().addAllEndDeviceEvents(endDeviceEvents);
        } else {
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            meterReading.addAllEndDeviceEvents(endDeviceEvents);
            this.meterReadings.put(deviceIdentifier.getIdentifier(), Pair.of(deviceIdentifier, meterReading));
        }
    }

    @Override
    public void addLastLogBookUpdater(LogBookIdentifier logBookIdentifier, Instant lastLogbook) {
        Instant existingLastLogBook = this.lastLogBooks.get(logBookIdentifier);
        if ((existingLastLogBook == null) || (lastLogbook != null && lastLogbook.isAfter(existingLastLogBook))) {
            this.lastLogBooks.put(logBookIdentifier, lastLogbook);
        }
    }

    public Map<String, Pair<DeviceIdentifier<Device>, MeterReadingImpl>> getMeterReadings(){
        return this.meterReadings;
    }

    public Map<LoadProfileIdentifier, Instant> getLastReadings(){
        return  this.lastReadings;
    }

    public Map<LogBookIdentifier, Instant> getLastLogBooks(){
        return this.lastLogBooks;
    }

    protected Optional<MeterDataStorageEvent> newEvent(List<Issue> issues) {
        MeterDataStorageEvent event  =  new MeterDataStorageEvent(new ComServerEventServiceProvider(), this);
        event.addIssues(issues);
        return Optional.of(event);
    }

    @Override
    public String getDescriptionTitle() {
        return DESCRIPTION_TITLE;
    }

}