/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.config.DeviceTypePurpose;
import com.energyict.mdc.device.data.Batch;
import com.energyict.mdc.device.data.Device;

import java.util.ArrayList;
import java.util.List;

import static com.elster.jupiter.util.Checks.is;

public class DataLoggerSlaveDeviceInfo {

    public long id;
    public String name;
    public String deviceTypeName;
    public String deviceTypePurpose; // Datalogger slave/ Multi-element Slave
    public long deviceConfigurationId;
    public long deviceTypeId;
    public String deviceConfigurationName;
    public String serialNumber;
    public String manufacturer;
    public String modelNbr;
    public String modelVersion;
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

    DataLoggerSlaveDeviceInfo(Device device){
        this.id = device.getId();
        this.name = device.getName();
        this.deviceTypeName = device.getDeviceType().getName();
        this.deviceTypeId = device.getDeviceType().getId();
        this.deviceTypePurpose = (device.getDeviceType().isDataloggerSlave() ? DeviceTypePurpose.DATALOGGER_SLAVE.name() : (device.getDeviceType().isMultiElementSlave() ? DeviceTypePurpose.MULTI_ELEMENT_SLAVE.name(): DeviceTypePurpose.REGULAR.name()));
        this.deviceConfigurationId = device.getDeviceConfiguration().getId();
        this.deviceConfigurationName = device.getDeviceConfiguration().getName();
        this.serialNumber = device.getSerialNumber();
        this.manufacturer = device.getManufacturer();
        this.modelNbr = device.getModelNumber();
        this.modelVersion = device.getManufacturer();
        this.yearOfCertification = device.getYearOfCertification();
        this.version = device.getVersion();
        this.fromExistingLink = true;
        this.batch = device.getBatch().map(Batch::getName).orElse(null);
        device.getLifecycleDates().getReceivedDate().ifPresent((shipmentDate) -> this.shipmentDate = shipmentDate.toEpochMilli());
    }
}
