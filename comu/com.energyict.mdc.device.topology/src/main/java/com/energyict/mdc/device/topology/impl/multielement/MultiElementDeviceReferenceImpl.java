/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl.multielement;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.DataLoggerChannelUsage;
import com.energyict.mdc.device.topology.impl.AbstractPhysicalGatewayReferenceImpl;
import com.energyict.mdc.device.topology.impl.DataLoggerChannelUsageImpl;
import com.energyict.mdc.device.topology.impl.MessageSeeds;
import com.energyict.mdc.device.topology.impl.PhysicalGatewayNotSameAsOrigin;
import com.energyict.mdc.device.topology.impl.utils.ChannelDataTransferor;
import com.energyict.mdc.device.topology.multielement.MultiElementDeviceReference;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@PhysicalGatewayNotSameAsOrigin(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DEVICE_CANNOT_BE_MULTI_ELEMENT_METER_FOR_ITSELF + "}")
@OriginDeviceTypeIsMultiElementSubmeter(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NOT_A_MULTI_ELEMENT_SUBMETER_DEVICE + "}")
@GatewayDeviceTypeIsMultiElementEnabled(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.GATEWAY_NOT_MULTI_ELEMENT_ENABLED + "}")
@AllSlaveChannelsIncluded(groups = {Save.Create.class}, message = "{" + MultiElementDeviceLinkException.NO_MAPPING_FOR_ALL_SLAVE_CHANNELS + "}")
@AllDataLoggerChannelsAvailable(groups = {Save.Create.class}, message = "{" + MessageSeeds.Keys.MULTI_ELEMENT_DEVICE_CHANNEL_ALREADY_REFERENCED + "}")
public class MultiElementDeviceReferenceImpl extends AbstractPhysicalGatewayReferenceImpl implements MultiElementDeviceReference {

    private List<DataLoggerChannelUsage> dataLoggerChannelUsages = new ArrayList<>();

    public MultiElementDeviceReferenceImpl createFor(Device origin, Device master, Interval interval) {
        super.createFor(origin, master, interval);
        return this;
    }

    @Override
    public boolean addChannelUsage(Channel slaveChannel, Channel dataLoggerChannel) {
        return dataLoggerChannelUsages.add(new DataLoggerChannelUsageImpl().createFor(this, slaveChannel, dataLoggerChannel));
    }

    @Override
    public List<DataLoggerChannelUsage> getChannelUsages() {
        return Collections.unmodifiableList(dataLoggerChannelUsages);
    }

    @Override
    public boolean isTerminated() {
        return getRange().hasUpperBound();
    }

    /**
     * Closes the current interval.
     */
    public void terminate(Instant closingDate, ChannelDataTransferor channelDataTransferor) {
        if (!isEffectiveAt(closingDate)) {
            throw new IllegalArgumentException(MessageSeeds.Keys.INVALID_TERMINATION_DATE);
        }

        // Data on the slave channels having a dat
        this.dataLoggerChannelUsages.stream().forEach((usage) -> channelDataTransferor.transferChannelDataToDataLogger(usage, closingDate));
        super.terminate(closingDate, channelDataTransferor);
    }

}
