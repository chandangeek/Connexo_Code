package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
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

    private final DeviceDataInfoFactory deviceDataInfoFactory;
    private final DataLoggerSlaveChannelInfoFactory slaveChannelInfoFactory;
    private final DataLoggerSlaveRegisterInfoFactory slaveRegisterInfoFactory;
    private final Clock clock;
    private final TopologyService topologyService;
    private DataLoggerSlaveDeviceInfo slaveDeviceInfoForUnlinkedDataLoggerElements;

    @Inject
    public DataLoggerSlaveDeviceInfoFactory(Clock clock, TopologyService topologyService ) {
        this.clock = clock;
        this.topologyService = topologyService;
        this.deviceDataInfoFactory = new DeviceDataInfoFactory(clock);
        this.slaveChannelInfoFactory = new DataLoggerSlaveChannelInfoFactory();
        this.slaveRegisterInfoFactory = new DataLoggerSlaveRegisterInfoFactory();
    }

    public List<DataLoggerSlaveDeviceInfo> from(Device dataLogger) {
        if (dataLogger.getDeviceConfiguration().isDataloggerEnabled()) {
            // DataLoggerSlaveDeviceInfo holding the unlinked data logger channels/registers
            slaveDeviceInfoForUnlinkedDataLoggerElements = new DataLoggerSlaveDeviceInfo();
            List<DataLoggerSlaveDeviceInfo> slaveDeviceInfos = new ArrayList<>();
            dataLogger.getChannels().stream().forEach((channel) -> addDataLoggerChannelInfos(channel, slaveDeviceInfos));
            dataLogger.getRegisters().stream().forEach((register) -> addDataLoggerRegisterInfos(register, slaveDeviceInfos));
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
            if (!slaveDeviceInfos.contains(slaveDeviceInfoForUnlinkedDataLoggerElements)){
                slaveDeviceInfos.add(slaveDeviceInfoForUnlinkedDataLoggerElements);
            }
            existingSlaveDeviceInfo = Optional.of(slaveDeviceInfoForUnlinkedDataLoggerElements);
        }
        existingSlaveDeviceInfo.get().addDataLoggerSlaveChannelInfo(slaveChannelInfo);
        return slaveDeviceInfos;
    }

    private List<DataLoggerSlaveDeviceInfo> addDataLoggerRegisterInfos(Register dataLoggerRegister, List<DataLoggerSlaveDeviceInfo> slaveDeviceInfos){
        Optional<Register> slaveRegister = topologyService.getSlaveRegister(dataLoggerRegister, clock.instant());
        Optional<DataLoggerSlaveDeviceInfo> existingSlaveDeviceInfo;
        DataLoggerSlaveRegisterInfo slaveRegisterInfo =  slaveRegisterInfoFactory.from(deviceDataInfoFactory.createRegisterInfo(dataLoggerRegister, null),
                slaveRegister.map((register) -> deviceDataInfoFactory.createRegisterInfo(dataLoggerRegister, null)));
        if (slaveRegister.isPresent()){
            Device slave = slaveRegister.get().getDevice();
            existingSlaveDeviceInfo = slaveDeviceInfos.stream().filter(slaveDeviceInfo -> slaveDeviceInfo.id == slave.getId()).findFirst();
            if (!existingSlaveDeviceInfo.isPresent()){
                DataLoggerSlaveDeviceInfo newSlaveDeviceInfo = DataLoggerSlaveDeviceInfo.from(slave);
                existingSlaveDeviceInfo = Optional.of(newSlaveDeviceInfo);
                slaveDeviceInfos.add(newSlaveDeviceInfo);
            }
        }else{
            if (!slaveDeviceInfos.contains(slaveDeviceInfoForUnlinkedDataLoggerElements)){
                slaveDeviceInfos.add(slaveDeviceInfoForUnlinkedDataLoggerElements);
            }
            existingSlaveDeviceInfo = Optional.of(slaveDeviceInfoForUnlinkedDataLoggerElements);
        }
        existingSlaveDeviceInfo.get().addDataLoggerSlaveRegisterInfo(slaveRegisterInfo);
        return slaveDeviceInfos;
    }

}
