package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.DataLoggerChannelUsage;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the link between a Data Logger and its gateway
 * Copyrights EnergyICT
 * Date: 10/03/14
 * Time: 09:57
 */
@PhysicalGatewayNotSameAsOrigin(groups = {Save.Create.class, Save.Update.class}, message = "{"+ MessageSeeds.Keys.DEVICE_CANNOT_BE_DATA_LOGGER_FOR_ITSELF +"}")
@OriginDeviceTypeIsDataLogger(groups = {Save.Create.class, Save.Update.class}, message = "{"+ MessageSeeds.Keys.NOT_A_DATALOGGER_SLAVE_DEVICE +"}")
@GatewayDeviceTypeIsDataLoggerEnabled(groups = {Save.Create.class, Save.Update.class}, message = "{"+ MessageSeeds.Keys.GATEWAY_NOT_DATALOGGER_ENABLED +"}")
@AllSlaveChannelsIncluded(groups = {Save.Create.class, Save.Update.class}, message = "{"+ MessageSeeds.Keys.NOT_ALL_SLAVE_CHANNELS_INCLUDED +"}")
public class DataLoggerReferenceImpl extends AbstractPhysicalGatewayReferenceImpl {

    private List<DataLoggerChannelUsage> dataLoggerChannelUsages = new ArrayList<>();

    public DataLoggerReferenceImpl createFor(Device origin, Device master, Interval interval) {
        super.createFor(origin, master, interval);
        return this;
    }

    public List<DataLoggerChannelUsage> getDataLoggerChannelUsages() {
        return dataLoggerChannelUsages;
    }

}