package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.topology.DataLoggerChannelUsage;
import com.energyict.mdc.device.topology.DataLoggerReference;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
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
@AllSlaveChannelsIncluded(groups = {Save.Create.class}, message = "{"+ MessageSeeds.Keys.NOT_ALL_SLAVE_CHANNELS_INCLUDED +"}")
@AllDataLoggerChannelsAvailable(groups = {Save.Create.class}, message = "{" + MessageSeeds.Keys.DATA_LOGGER_CHANNEL_ALREADY_REFERENCED +"}")
public class DataLoggerReferenceImpl extends AbstractPhysicalGatewayReferenceImpl implements DataLoggerReference {

    private List<DataLoggerChannelUsage> dataLoggerChannelUsages = new ArrayList<>();

    public DataLoggerReferenceImpl createFor(Device origin, Device master, Interval interval) {
        super.createFor(origin, master, interval);
        return this;
    }

    public boolean addDataLoggerChannelUsage(Channel slaveChannel, Channel dataLoggerChannel){
         return dataLoggerChannelUsages.add(new DataLoggerChannelUsageImpl().createFor(this, slaveChannel, dataLoggerChannel));
    }

    public List<DataLoggerChannelUsage> getDataLoggerChannelUsages() {
        return Collections.unmodifiableList(dataLoggerChannelUsages);
    }

    /**
     * Data from each DataLogger Channel is transferred to the slave channel for this DataLoggerReference's interval
     */
    void transferChannelDataToSlave(TopologyServiceImpl topologyService){
        this.dataLoggerChannelUsages.stream().forEach((channelUsage) -> transferChannelDataToSlave(topologyService, channelUsage));
    }

    /**
     * Closes the current interval.
     */
    public void terminate(Instant closingDate){
        // Data on the slave channels having a dat
        this.dataLoggerChannelUsages.stream().forEach((usage) -> transferChannelDataToDataLogger(usage, closingDate));
        this.getOrigin().activate(closingDate);   //current Meter Activation is stopped and a new one is started
        super.terminate(closingDate);
    }

    private void transferChannelDataToSlave(TopologyServiceImpl topologyService, DataLoggerChannelUsage channelUsage){
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();

        Channel dataloggerChannel = channelUsage.getDataLoggerChannel();
        Channel slaveChannel = channelUsage.getSlaveChannel();

        if (dataloggerChannel.hasData()) {
            if (dataloggerChannel.isRegular()) {
                List<IntervalReading> readings = new ArrayList<>();
                readings.addAll(dataloggerChannel.getIntervalReadings(getInterval().toOpenClosedRange()));
                if (readings.isEmpty()) {
                    return;
                }
                IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(slaveChannel.getMainReadingType().getMRID());
                intervalBlock.addAllIntervalReadings(readings);
                meterReading.addAllIntervalBlocks(Collections.singletonList(intervalBlock));

            } else {
                List<Reading> readings = new ArrayList<>();
                readings.addAll(dataloggerChannel.getRegisterReadings(this.getRange()));
                if (readings.isEmpty()) {
                    return;
                }
                meterReading.addAllReadings(readings);
            }
            this.getOrigin().store(meterReading);
            if (dataloggerChannel.isRegular()){
                updateLastReadingIfApplicable(topologyService, slaveChannel);
            }
        }
    }

    private void updateLastReadingIfApplicable(TopologyServiceImpl topologyService, Channel channel){
        LoadProfile toUpdate =  topologyService.getChannel(this.getOrigin(), channel).map(com.energyict.mdc.device.data.Channel::getLoadProfile).get();
        if (!toUpdate.getLastReading().isPresent() || toUpdate.getLastReading().get().isBefore(channel.getLastDateTime())) {
            this.getOrigin().getLoadProfileUpdaterFor(toUpdate).setLastReading(channel.getLastDateTime()).update();
        }
    }

    private void transferChannelDataToDataLogger(DataLoggerChannelUsage channelUsage, Instant start){
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        // Make sure we are using the current Meter Activation channels instances
        Channel slaveChannel = getOrigin().getCurrentMeterActivation().get().getChannels().stream().filter((each) -> each.getId() ==  channelUsage.getSlaveChannel().getId()).findFirst().get();
        Channel dataloggerChannel = channelUsage.getDataLoggerChannel();
        if (slaveChannel.hasData()) {
            if (slaveChannel.isRegular()) {
                List<IntervalReading> readings = new ArrayList<>();
                readings.addAll(slaveChannel.getIntervalReadings(Range.openClosed(start, slaveChannel.getLastDateTime())));
                if (readings.isEmpty()) {
                    return;
                }
                IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(dataloggerChannel.getMainReadingType().getMRID());
                intervalBlock.addAllIntervalReadings(readings);
                meterReading.addAllIntervalBlocks(Collections.singletonList(intervalBlock));
                slaveChannel.removeReadings(slaveChannel.getIntervalReadings(Range.greaterThan(start)));
            } else {
                List<ReadingRecord> readings = new ArrayList<>();
                readings.addAll(slaveChannel.getRegisterReadings(Range.atLeast(start)));
                if (readings.isEmpty()) {
                    return;
                }
                meterReading.addAllReadings(readings);
                slaveChannel.removeReadings(readings);
            }
            this.getGateway().store(meterReading);
        }
    }
}