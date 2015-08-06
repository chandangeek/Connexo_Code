package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.readings.EndDeviceEvent;
import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.Reading;
import com.energyict.mdc.common.comserver.logging.CanProvideDescriptionTitle;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;

import java.time.Instant;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 05.08.15
 * Time: 16:09
 */
public interface MeterDataStoreCommand extends DeviceCommand, CanProvideDescriptionTitle {

    void addIntervalReadings(DeviceIdentifier<Device> deviceIdentifier, List<IntervalBlock> intervalBlocks);

    void addLastReadingUpdater(LoadProfileIdentifier loadProfileIdentifier, Instant lastReading);

    void addReadings(DeviceIdentifier<Device> deviceIdentifier, List<Reading> registerReadings);

    void addEventReadings(DeviceIdentifier<Device> deviceIdentifier, List<EndDeviceEvent> endDeviceEvents);

    void addLastLogBookUpdater(LogBookIdentifier logBookIdentifier, Instant lastLogbook);

    ServiceProvider getServiceProvider();
}
