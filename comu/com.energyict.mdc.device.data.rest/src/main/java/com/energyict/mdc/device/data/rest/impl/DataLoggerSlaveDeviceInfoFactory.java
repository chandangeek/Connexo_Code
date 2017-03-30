/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.topology.TopologyService;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DataLoggerSlaveDeviceInfoFactory {

    private volatile DeviceDataInfoFactory deviceDataInfoFactory;
    private volatile DataLoggerSlaveChannelInfoFactory slaveChannelInfoFactory;
    private volatile DataLoggerSlaveRegisterInfoFactory slaveRegisterInfoFactory;
    private volatile Clock clock;
    private volatile TopologyService topologyService;
    private volatile BatchService batchService;
    private DataLoggerSlaveDeviceInfo slaveDeviceInfoForUnlinkedDataLoggerElements;
    private final ChannelInfoFactory channelInfoFactory;

    @Inject
    public DataLoggerSlaveDeviceInfoFactory(Clock clock,
                                            TopologyService topologyService,
                                            DeviceDataInfoFactory deviceDataInfoFactory,
                                            BatchService batchService,
                                            ChannelInfoFactory channelInfoFactory) {
        this.clock = clock;
        this.topologyService = topologyService;
        this.deviceDataInfoFactory = deviceDataInfoFactory;
        this.slaveChannelInfoFactory = new DataLoggerSlaveChannelInfoFactory();
        this.slaveRegisterInfoFactory = new DataLoggerSlaveRegisterInfoFactory();
        this.batchService = batchService;
        this.channelInfoFactory = channelInfoFactory;
    }

    public List<DataLoggerSlaveDeviceInfo> from(Device dataLogger) {
        if (dataLogger.getDeviceConfiguration().isDataloggerEnabled()) {
            // DataLoggerSlaveDeviceInfo holding the unlinked data logger channels/registers
            slaveDeviceInfoForUnlinkedDataLoggerElements = new DataLoggerSlaveDeviceInfo();
            List<DataLoggerSlaveDeviceInfo> slaveDeviceInfos = new ArrayList<>();
            dataLogger.getChannels().stream().forEach((channel) -> addDataLoggerChannelInfo(channel, slaveDeviceInfos));
            dataLogger.getRegisters().stream().forEach((register) -> addDataLoggerRegisterInfo(register, slaveDeviceInfos));
            return slaveDeviceInfos;
        }
        return null;
    }

    private List<DataLoggerSlaveDeviceInfo> addDataLoggerChannelInfo(Channel dataLoggerChannel, List<DataLoggerSlaveDeviceInfo> slaveDeviceInfos){
        Instant checkPoint = clock.instant();
        Optional<Channel> slaveChannel = topologyService.getSlaveChannel(dataLoggerChannel,checkPoint);
        Optional<DataLoggerSlaveDeviceInfo> existingSlaveDeviceInfo;
        DataLoggerSlaveChannelInfo slaveChannelInfo = slaveChannelInfoFactory.from(channelInfoFactory.from(dataLoggerChannel), slaveChannel.map(channelInfoFactory::from));
        if (slaveChannel.isPresent()){
            Device slave = slaveChannel.get().getDevice();

            existingSlaveDeviceInfo = slaveDeviceInfos.stream().filter(slaveDeviceInfo -> slaveDeviceInfo.id == slave.getId()).findFirst();
            if (!existingSlaveDeviceInfo.isPresent()){
                DataLoggerSlaveDeviceInfo newSlaveDeviceInfo = DataLoggerSlaveDeviceInfo.from(slave, batchService, topologyService, clock);
                existingSlaveDeviceInfo = Optional.of(newSlaveDeviceInfo);
                slaveDeviceInfos.add(newSlaveDeviceInfo);
            }
        }else{
            if (!slaveDeviceInfos.contains(slaveDeviceInfoForUnlinkedDataLoggerElements)){
                slaveDeviceInfos.add(slaveDeviceInfoForUnlinkedDataLoggerElements);
            }
            topologyService.availabilityDate(dataLoggerChannel).ifPresent((when) -> slaveChannelInfo.availabilityDate = when.toEpochMilli());
            existingSlaveDeviceInfo = Optional.of(slaveDeviceInfoForUnlinkedDataLoggerElements);
        }
        existingSlaveDeviceInfo.get().addDataLoggerSlaveChannelInfo(slaveChannelInfo);
        return slaveDeviceInfos;
    }

    private List<DataLoggerSlaveDeviceInfo> addDataLoggerRegisterInfo(Register dataLoggerRegister, List<DataLoggerSlaveDeviceInfo> slaveDeviceInfos){
        Optional<Register> slaveRegister = topologyService.getSlaveRegister(dataLoggerRegister, clock.instant());
        Optional<DataLoggerSlaveDeviceInfo> existingSlaveDeviceInfo;
        DataLoggerSlaveRegisterInfo slaveRegisterInfo = slaveRegisterInfoFactory.from(deviceDataInfoFactory.createRegisterInfo(dataLoggerRegister, null, topologyService),
                slaveRegister.map((register) -> deviceDataInfoFactory.createRegisterInfo(register, null, topologyService)));
        if (slaveRegister.isPresent()){
            Device slave = slaveRegister.get().getDevice();
            existingSlaveDeviceInfo = slaveDeviceInfos.stream().filter(slaveDeviceInfo -> slaveDeviceInfo.id == slave.getId()).findFirst();
            if (!existingSlaveDeviceInfo.isPresent()){
                DataLoggerSlaveDeviceInfo newSlaveDeviceInfo = DataLoggerSlaveDeviceInfo.from(slave, batchService, topologyService, clock);
                existingSlaveDeviceInfo = Optional.of(newSlaveDeviceInfo);
                slaveDeviceInfos.add(newSlaveDeviceInfo);
            }
        }else{
            if (!slaveDeviceInfos.contains(slaveDeviceInfoForUnlinkedDataLoggerElements)){
                slaveDeviceInfos.add(slaveDeviceInfoForUnlinkedDataLoggerElements);
            }
            topologyService.availabilityDate(dataLoggerRegister).ifPresent((when) -> slaveRegisterInfo.availabilityDate = when.toEpochMilli());
            existingSlaveDeviceInfo = Optional.of(slaveDeviceInfoForUnlinkedDataLoggerElements);
        }
        existingSlaveDeviceInfo.get().addDataLoggerSlaveRegisterInfo(slaveRegisterInfo);
        return slaveDeviceInfos;
    }

    public DataLoggerSlaveDeviceInfos forDataLoggerSlaves(List<Device> devices) {
        DataLoggerSlaveDeviceInfos dataLoggerSlaveDeviceInfos = new DataLoggerSlaveDeviceInfos(topologyService, clock, batchService);
        dataLoggerSlaveDeviceInfos.addAll(devices);
        return dataLoggerSlaveDeviceInfos;
    }
}
