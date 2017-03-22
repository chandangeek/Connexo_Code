/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.DataLoggerChannelUsage;
import com.energyict.mdc.device.topology.DataLoggerReference;
import com.energyict.mdc.device.topology.impl.utils.ChannelDataTransferor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@PhysicalGatewayNotSameAsOrigin(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DEVICE_CANNOT_BE_DATA_LOGGER_FOR_ITSELF + "}")
@OriginDeviceTypeIsDataLogger(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NOT_A_DATALOGGER_SLAVE_DEVICE + "}")
@GatewayDeviceTypeIsDataLoggerEnabled(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.GATEWAY_NOT_DATALOGGER_ENABLED + "}")
@AllSlaveChannelsIncluded(groups = {Save.Create.class}, message = "{" + DataLoggerLinkException.NO_MAPPING_FOR_ALL_SLAVE_CHANNELS + "}")
@AllDataLoggerChannelsAvailable(groups = {Save.Create.class}, message = "{" + MessageSeeds.Keys.DATA_LOGGER_CHANNEL_ALREADY_REFERENCED + "}")
public class DataLoggerReferenceImpl extends AbstractPhysicalGatewayReferenceImpl implements DataLoggerReference {

    private List<DataLoggerChannelUsage> dataLoggerChannelUsages = new ArrayList<>();

    public DataLoggerReferenceImpl createFor(Device origin, Device master, Interval interval) {
        super.createFor(origin, master, interval);
        return this;
    }

    @Override
    public boolean addDataLoggerChannelUsage(Channel slaveChannel, Channel dataLoggerChannel) {
        return dataLoggerChannelUsages.add(new DataLoggerChannelUsageImpl().createFor(this, slaveChannel, dataLoggerChannel));
    }

    public List<DataLoggerChannelUsage> getDataLoggerChannelUsages() {
        return Collections.unmodifiableList(dataLoggerChannelUsages);
    }

    @Override
    public boolean isTerminated() {
        return getRange().hasUpperBound();
    }

    /**
     * Closes the current interval.
     */
    public void terminate(Instant closingDate) {
        if (!isEffectiveAt(closingDate)) {
            throw new IllegalArgumentException(MessageSeeds.Keys.INVALID_TERMINATION_DATE);
            //throw DataLoggerLinkException.invalidTerminationDate(thesaurus);
        }

        ChannelDataTransferor channelDataTransferor = new ChannelDataTransferor();
        // Data on the slave channels having a dat
        this.dataLoggerChannelUsages.stream().forEach((usage) -> channelDataTransferor.transferChannelDataToDataLogger(usage, closingDate));
        super.terminate(closingDate);
    }
}
