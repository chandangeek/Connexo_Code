/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.Batch;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.TopologyService;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import static com.elster.jupiter.util.Checks.is;

public class DataLoggerSlaveDeviceInfo {

    public long id;
    public String name;
    public String deviceTypeName;
    public long deviceConfigurationId;
    public long deviceTypeId;
    public String deviceConfigurationName;
    public String serialNumber;
    public int yearOfCertification;
    public long version;
    public long shipmentDate;
    public Long linkingTimeStamp;
    public long unlinkingTimeStamp = -1L;
    public String batch;
    private boolean fromExistingLink;      // as indicator that this dataloggerSlaveDeviceInfo was created when creating its parent Device Info (Should not be taken into account whe
                                            // updating the device (with an updated device info)

    public List<DataLoggerSlaveChannelInfo> dataLoggerSlaveChannelInfos;   //mapping slave channel to data logger channel
    public List<DataLoggerSlaveRegisterInfo> dataLoggerSlaveRegisterInfos;   //mapping slave register to data logger register

    DataLoggerSlaveDeviceInfo(){
        super();
    }

    public boolean addDataLoggerSlaveChannelInfo(DataLoggerSlaveChannelInfo slaveChannelInfo){
        if (dataLoggerSlaveChannelInfos == null){
            dataLoggerSlaveChannelInfos = new ArrayList<>();
        }
        return dataLoggerSlaveChannelInfos.add(slaveChannelInfo);
    }

    public boolean addDataLoggerSlaveRegisterInfo(DataLoggerSlaveRegisterInfo slaveRegisterInfo){
        if (dataLoggerSlaveRegisterInfos == null){
            dataLoggerSlaveRegisterInfos = new ArrayList<>();
        }
        return dataLoggerSlaveRegisterInfos.add(slaveRegisterInfo);
    }

    // A DataLoggerSlaveDeviceInfo with only a list of {@link DataLoggerSlaveChannelInfo}s
    // and or a list of {@link DataLoggerSlaveRegisterInfo}s is used
    // for keeping the data logger's channels and registers which are not linked yet
    public boolean placeHolderForUnlinkedDataLoggerChannelsAndRegisters(){
        return is(name).emptyOrOnlyWhiteSpace();
    }

    public boolean unlinked(){
        return unlinkingTimeStamp >= 0;
    }

    public boolean isFromExistingLink(){
        return fromExistingLink;
    }

    static DataLoggerSlaveDeviceInfo from(Device device, BatchService batchService, TopologyService topologyService, Clock clock) {
        DataLoggerSlaveDeviceInfo info = new DataLoggerSlaveDeviceInfo();
        info.id = device.getId();
        info.name = device.getName();
        info.deviceTypeName = device.getDeviceType().getName();
        info.deviceTypeId = device.getDeviceType().getId();
        info.deviceConfigurationId = device.getDeviceConfiguration().getId();
        info.deviceConfigurationName = device.getDeviceConfiguration().getName();
        info.serialNumber = device.getSerialNumber();
        info.yearOfCertification = device.getYearOfCertification();
        info.version = device.getVersion();
        info.fromExistingLink = true;
        info.batch = device.getBatch().map(Batch::getName).orElse(null);
        info.linkingTimeStamp = topologyService.findDataloggerReference(device, clock.instant())
                .map(dataLoggerReference -> dataLoggerReference.getRange().lowerEndpoint().toEpochMilli())
                .orElse(null);
        topologyService.findLastDataloggerReference(device).ifPresent(dataLoggerReference -> {
            if (dataLoggerReference.isTerminated()) {
                info.unlinkingTimeStamp = dataLoggerReference.getRange().upperEndpoint().toEpochMilli();
            }
        });
        device.getLifecycleDates().getReceivedDate().ifPresent((shipmentDate) -> info.shipmentDate = shipmentDate.toEpochMilli());
        return info;
    }

}
