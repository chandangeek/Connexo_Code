/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component(name = "com.energyict.mdc.device.topology.impl.DataLoggerReferenceMeterActivationHandler", service = TopicHandler.class, immediate = true)
public class DataLoggerReferenceMeterActivationHandler implements TopicHandler {

    private static final String TOPIC = "com/energyict/mdc/device/data/meteractivation/RESTARTED";

    private volatile TopologyService topologyService;

    public DataLoggerReferenceMeterActivationHandler() {
    }

    @Inject
    public DataLoggerReferenceMeterActivationHandler(TopologyService topologyService) {
        this();
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
                    .ifPresent(dataLoggerReference -> createNewDataLoggerReferenceIfApplicable(dataLoggerReference, currentMeterActivation, SourceDeviceType.DATALOGGER)));

            //Do it for the case where it is a slave
            topologyService.findDataloggerReference(device, currentMeterActivation.getStart())
                    .ifPresent(dataLoggerReference -> createNewDataLoggerReferenceIfApplicable(dataLoggerReference, currentMeterActivation, SourceDeviceType.SLAVE));
        });
    }

    private void createNewDataLoggerReferenceIfApplicable(DataLoggerReference dataLoggerReference, MeterActivation currentMeterActivation, SourceDeviceType deviceType) {
        if (!dataLoggerReference.isTerminated()) {
            Map<Channel, Channel> channelMap = new HashMap<>();
            Map<Register, Register> registerMap = new HashMap<>();
            dataLoggerReference
                .getDataLoggerChannelUsages()
                .forEach(dataLoggerChannelUsage ->
                    currentMeterActivation
                        .getChannelsContainer()
                        .getChannels()
                        .stream()
                        .filter(channel -> {
                                List<ReadingType> commonReadingTypes = new ArrayList<>(channel.getReadingTypes());
                                commonReadingTypes.retainAll(deviceType.getReadingTypes(dataLoggerChannelUsage));
                                return !commonReadingTypes.isEmpty();
                            })
                        .findAny()
                        .ifPresent(newChannel ->
                                mapToProperMdcChannelsAndRegisters(
                                        dataLoggerReference.getGateway(),
                                        dataLoggerReference.getOrigin(),
                                        channelMap,
                                        registerMap,
                                        dataLoggerChannelUsage,
                                        newChannel,
                                        deviceType)));
            topologyService.clearDataLogger(dataLoggerReference.getOrigin(), currentMeterActivation.getStart());
            topologyService.setDataLogger(
                    dataLoggerReference.getOrigin(),
                    dataLoggerReference.getGateway(),
                    currentMeterActivation.getStart(),
                    channelMap,
                    registerMap);
        }
    }

    private void mapToProperMdcChannelsAndRegisters(Device dataLogger, Device slave, Map<Channel, Channel> channelMap, Map<Register, Register> registerMap, DataLoggerChannelUsage dataLoggerChannelUsage, com.elster.jupiter.metering.Channel newChannel, SourceDeviceType deviceType) {
        if (newChannel.isRegular()) {
            Optional<Channel> dataLoggerChannel = findMdcChannel(dataLogger, deviceType.pickDataLoggerChannelFrom(newChannel, dataLoggerChannelUsage));
            Optional<Channel> slaveChannel = findMdcChannel(slave, deviceType.pickSlaveChannelFrom(newChannel, dataLoggerChannelUsage));
            if (dataLoggerChannel.isPresent() && slaveChannel.isPresent()) {
                channelMap.put(slaveChannel.get(), dataLoggerChannel.get());
            }
        } else {
            Optional<Register> dataLoggerRegister = findMdcRegister(dataLogger, deviceType.pickDataLoggerChannelFrom(newChannel, dataLoggerChannelUsage));
            Optional<Register> slaveRegister = findMdcRegister(slave, deviceType.pickSlaveChannelFrom(newChannel, dataLoggerChannelUsage));
            if (dataLoggerRegister.isPresent() && slaveRegister.isPresent()) {
                registerMap.put(slaveRegister.get(), dataLoggerRegister.get());
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

    /**
     * Contains behavior that extracts data from {@link DataLoggerChannelUsage}
     * that will be different when the source of the event
     * is a data logger or a slave device.
     */
    private enum SourceDeviceType {
        DATALOGGER {
            @Override
            List<? extends ReadingType> getReadingTypes(DataLoggerChannelUsage dataLoggerChannelUsage) {
                return dataLoggerChannelUsage.getDataLoggerChannel().getReadingTypes();
            }

            @Override
            com.elster.jupiter.metering.Channel pickDataLoggerChannelFrom(com.elster.jupiter.metering.Channel newChannel, DataLoggerChannelUsage dataLoggerChannelUsage) {
                return newChannel;
            }

            @Override
            com.elster.jupiter.metering.Channel pickSlaveChannelFrom(com.elster.jupiter.metering.Channel newChannel, DataLoggerChannelUsage dataLoggerChannelUsage) {
                return dataLoggerChannelUsage.getSlaveChannel();
            }
        },

        SLAVE {
            @Override
            List<? extends ReadingType> getReadingTypes(DataLoggerChannelUsage dataLoggerChannelUsage) {
                return dataLoggerChannelUsage.getSlaveChannel().getReadingTypes();
            }

            @Override
            com.elster.jupiter.metering.Channel pickDataLoggerChannelFrom(com.elster.jupiter.metering.Channel newChannel, DataLoggerChannelUsage dataLoggerChannelUsage) {
                return dataLoggerChannelUsage.getDataLoggerChannel();
            }

            @Override
            com.elster.jupiter.metering.Channel pickSlaveChannelFrom(com.elster.jupiter.metering.Channel newChannel, DataLoggerChannelUsage dataLoggerChannelUsage) {
                return newChannel;
            }
        };

        abstract List<? extends ReadingType> getReadingTypes(DataLoggerChannelUsage dataLoggerChannelUsage);

        abstract com.elster.jupiter.metering.Channel pickDataLoggerChannelFrom(com.elster.jupiter.metering.Channel newChannel, DataLoggerChannelUsage dataLoggerChannelUsage);

        abstract com.elster.jupiter.metering.Channel pickSlaveChannelFrom(com.elster.jupiter.metering.Channel newChannel, DataLoggerChannelUsage dataLoggerChannelUsage);

    }

}