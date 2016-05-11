package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.TopologyService;

import javax.inject.Inject;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 10/05/2016
 * Time: 11:29
 */
public class DataLoggerSlaveDeviceInfoFactory {

    private final DataLoggerSlaveChannelInfoFactory slaveChannelInfoFactory;
    private final Clock clock;
    private final TopologyService topologyService;
    private DataLoggerSlaveDeviceInfo slaveDeviceInfoForUnlinkedDataLoggerChannels;


    @Inject
    public DataLoggerSlaveDeviceInfoFactory(Clock clock, TopologyService topologyService) {
        this.clock = clock;
        this.topologyService = topologyService;
        slaveChannelInfoFactory = new DataLoggerSlaveChannelInfoFactory();
    }

    public List<DataLoggerSlaveDeviceInfo> from(Device dataLogger) {
        if (dataLogger.getDeviceConfiguration().isDataloggerEnabled()) {
            // DataLoggerSlaveDeviceInfo holding the unlinked data logger channels
            slaveDeviceInfoForUnlinkedDataLoggerChannels = new DataLoggerSlaveDeviceInfo();
            List<DataLoggerSlaveDeviceInfo> slaveDeviceInfos = new ArrayList<>();
            dataLogger.getChannels().stream().forEach((channel) -> addDataLoggerChannelInfos(channel, slaveDeviceInfos));
            return slaveDeviceInfos;
        }
        return null;
    }

    private List<DataLoggerSlaveDeviceInfo> addDataLoggerChannelInfos(Channel dataLoggerChannel, List<DataLoggerSlaveDeviceInfo> slaveDeviceInfos){
        Optional<Channel> slaveChannel = topologyService.getSlaveChannel(dataLoggerChannel, clock.instant());
        Optional<DataLoggerSlaveDeviceInfo> existingSlaveDeviceInfo;
        DataLoggerSlaveChannelInfo slaveChannelInfo =  slaveChannelInfoFactory.from(ChannelInfo.from(dataLoggerChannel, clock), slaveChannel.map((channel) -> ChannelInfo.from(channel, clock)));
        if (slaveChannel.isPresent()){
            Device slave = slaveChannel.get().getDevice();
            existingSlaveDeviceInfo = slaveDeviceInfos.stream().filter(slaveDeviceInfo -> slaveDeviceInfo.id == slave.getId()).findFirst();
            if (!existingSlaveDeviceInfo.isPresent()){
                DataLoggerSlaveDeviceInfo newSlaveDeviceInfo = DataLoggerSlaveDeviceInfo.from(slave);
                existingSlaveDeviceInfo = Optional.of(newSlaveDeviceInfo);
                slaveDeviceInfos.add(newSlaveDeviceInfo);
            }
        }else{
            if (!slaveDeviceInfos.contains(slaveDeviceInfoForUnlinkedDataLoggerChannels)){
                slaveDeviceInfos.add(slaveDeviceInfoForUnlinkedDataLoggerChannels);
            }
            existingSlaveDeviceInfo = Optional.of(slaveDeviceInfoForUnlinkedDataLoggerChannels);
        }
        existingSlaveDeviceInfo.get().addDataLoggerSlaveChannelInfo(slaveChannelInfo);
        return slaveDeviceInfos;
    }


}
