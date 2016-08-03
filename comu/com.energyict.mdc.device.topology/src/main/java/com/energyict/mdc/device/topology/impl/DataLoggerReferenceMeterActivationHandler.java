package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.topology.DataLoggerChannelUsage;
import com.energyict.mdc.device.topology.DataLoggerReference;
import com.energyict.mdc.device.topology.TopologyService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 03.08.16
 * Time: 14:30
 */
@Component(name = "com.energyict.mdc.device.topology.impl.DataLoggerReferenceMeterActivationHandler", service = TopicHandler.class, immediate = true)
public class DataLoggerReferenceMeterActivationHandler implements TopicHandler {

    private static final String TOPIC = "com/energyict/mdc/device/data/meteractivation/RESTARTED";

    private volatile TopologyService topologyService;

    public DataLoggerReferenceMeterActivationHandler() {
    }

    public DataLoggerReferenceMeterActivationHandler(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        Device device = (Device) localEvent.getSource();
        device.getCurrentMeterActivation().ifPresent(currentMeterActivation -> {

            //Do it for the case where it is the dataLogger
            List<Device> dataLoggerSlaves = topologyService.findDataLoggerSlaves(device);
            dataLoggerSlaves.forEach(slave -> topologyService.findLastDataloggerReference(slave)
                    .ifPresent(dataLoggerReference -> createNewDataLoggerReferenceIfApplicable(dataLoggerReference, currentMeterActivation)));

            //Do it for the case where it is a slave
            topologyService.findDataloggerReference(device, currentMeterActivation.getStart())
                    .ifPresent(dataLoggerReference -> createNewDataLoggerReferenceIfApplicable(dataLoggerReference, currentMeterActivation));
        });
    }

    private void createNewDataLoggerReferenceIfApplicable(DataLoggerReference dataLoggerReference, MeterActivation currentMeterActivation) {
        if (!dataLoggerReference.isTerminated()) {
            Map<Channel, Channel> channelMap = new HashMap<>();
            Map<Register, Register> registerMap = new HashMap<>();
            dataLoggerReference.getDataLoggerChannelUsages().forEach(dataLoggerChannelUsage -> {

                Optional<com.elster.jupiter.metering.Channel> newDataLoggerChannel = currentMeterActivation.getChannelsContainer().getChannels().stream().filter(channel -> {
                    ArrayList<ReadingType> commonReadingTypes = new ArrayList<>(channel.getReadingTypes());
                    commonReadingTypes.retainAll(dataLoggerChannelUsage.getDataLoggerChannel().getReadingTypes());
                    return commonReadingTypes.size() > 0;
                }).findAny();

                if (newDataLoggerChannel.isPresent()) {
                    mapToProperMdcChannelsAndRegisters(dataLoggerReference.getGateway(), dataLoggerReference.getOrigin(), channelMap, registerMap, dataLoggerChannelUsage, newDataLoggerChannel);
                }

            });
            topologyService.clearDataLogger(dataLoggerReference.getOrigin(), currentMeterActivation.getStart());
            topologyService.setDataLogger(dataLoggerReference.getOrigin(), dataLoggerReference.getGateway(), currentMeterActivation.getStart(), channelMap, registerMap);
        }
    }

    private void mapToProperMdcChannelsAndRegisters(Device device, Device slave, Map<Channel, Channel> channelMap, Map<Register, Register> registerMap, DataLoggerChannelUsage dataLoggerChannelUsage, Optional<com.elster.jupiter.metering.Channel> newDataLoggerChannel) {
        if (newDataLoggerChannel.get().isRegular()) {
            Optional<Channel> dataLoggerChannel = findMdcChannel(device, newDataLoggerChannel.get());
            Optional<Channel> slaveChannel = findMdcChannel(slave, dataLoggerChannelUsage.getSlaveChannel());
            if (dataLoggerChannel.isPresent() && slaveChannel.isPresent()) {
                channelMap.put(dataLoggerChannel.get(), slaveChannel.get());
            }
        } else {
            Optional<Register> dataLoggerRegister = findMdcRegister(device, newDataLoggerChannel.get());
            Optional<Register> slaveRegister = findMdcRegister(slave, dataLoggerChannelUsage.getSlaveChannel());
            if (dataLoggerRegister.isPresent() && slaveRegister.isPresent()) {
                registerMap.put(dataLoggerRegister.get(), slaveRegister.get());
            }
        }
    }

    private Optional<Channel> findMdcChannel(Device device, com.elster.jupiter.metering.Channel channel) {
        return device.getChannels().stream().filter(mdcChannel -> channel.getReadingTypes().contains(mdcChannel.getReadingType())).findAny();
    }

    private Optional<Register> findMdcRegister(Device device, com.elster.jupiter.metering.Channel channel) {
        return device.getRegisters().stream().filter(mdcRegister -> channel.getReadingTypes().contains(mdcRegister.getReadingType())).findAny();
    }

    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }
}
