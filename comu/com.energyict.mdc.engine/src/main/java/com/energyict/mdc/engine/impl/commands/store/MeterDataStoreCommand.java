package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.readings.EndDeviceEvent;
import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyrights EnergyICT
 * Date: 7/31/14
 * Time: 3:44 PM
 */
public class MeterDataStoreCommand extends DeviceCommandImpl {

    private final Map<String, Pair<DeviceIdentifier<Device>, MeterReadingImpl>> meterReadings = new HashMap<>();
    private final Map<LoadProfileIdentifier, Date> lastReadings = new HashMap<>();
    private final Map<LogBookIdentifier, Date> lastLogBooks = new HashMap<>();

    @Override
    protected void doExecute(ComServerDAO comServerDAO) {
        for (Map.Entry<String, Pair<DeviceIdentifier<Device>, MeterReadingImpl>> deviceMeterReadingEntry : meterReadings.entrySet()) {
            comServerDAO.storeMeterReadings(deviceMeterReadingEntry.getValue().getFirst(), deviceMeterReadingEntry.getValue().getLast());
        }

        for (Map.Entry<LoadProfileIdentifier, Date> loadProfileDateEntry : lastReadings.entrySet()) {
            comServerDAO.updateLastReadingFor(loadProfileDateEntry.getKey(), loadProfileDateEntry.getValue());
        }

        for (Map.Entry<LogBookIdentifier, Date> logBookDateEntry : lastLogBooks.entrySet()) {
            comServerDAO.updateLastLogBook(logBookDateEntry.getKey(), logBookDateEntry.getValue());
        }
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {

    }

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

    public void addLastReadingUpdater(LoadProfileIdentifier loadProfileIdentifier, Date lastReading) {
        Date existingLastReading = this.lastReadings.get(loadProfileIdentifier);
        if ((existingLastReading == null) || (lastReading != null && lastReading.after(existingLastReading))) {
            this.lastReadings.put(loadProfileIdentifier, lastReading);
        }
    }

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

    public void addLastLogBookUpdater(LogBookIdentifier logBookIdentifier, Date lastLogbook) {
        Date existingLastLogBook = this.lastLogBooks.get(logBookIdentifier);
        if ((existingLastLogBook == null) || (lastLogbook != null && lastLogbook.after(existingLastLogBook))) {
            this.lastLogBooks.put(logBookIdentifier, lastLogbook);
        }
    }
}
