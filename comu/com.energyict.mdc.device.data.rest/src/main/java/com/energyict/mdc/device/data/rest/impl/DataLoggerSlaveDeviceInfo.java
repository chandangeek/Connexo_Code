package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.Device;

import java.util.ArrayList;
import java.util.List;

import static com.elster.jupiter.util.Checks.is;

/**
 * Slave devices linked to a data logger : rest counterpart of {@link com.energyict.mdc.device.topology.impl.DataLoggerReferenceImpl}
 * Copyrights EnergyICT
 * Date: 10/05/2016
 * Time: 10:41
 */
public class DataLoggerSlaveDeviceInfo {

    public long id;
    public String mRID;
    public String deviceTypeName;
    public long deviceConfigurationId;
    public String deviceConfigurationName;
    public String serialNumber;
    public int yearOfCertification;
    public long version;
    public long shipmentTimeStamp;
    public long linkingTimeStamp;
    public long unlinkingTimeStamp = -1L;
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
        return is(mRID).emptyOrOnlyWhiteSpace();
    }

    public boolean unlinked(){
        return unlinkingTimeStamp >= 0;
    }

    public boolean isFromExistingLink(){
        return fromExistingLink;
    }

    static DataLoggerSlaveDeviceInfo from(Device device) {
        DataLoggerSlaveDeviceInfo info = new DataLoggerSlaveDeviceInfo();
        info.id = device.getId();
        info.mRID = device.getmRID();
        info.deviceTypeName = device.getDeviceType().getName();
        info.deviceConfigurationId = device.getDeviceConfiguration().getId();
        info.deviceConfigurationName = device.getDeviceConfiguration().getName();
        info.serialNumber = device.getSerialNumber();
        info.yearOfCertification = device.getYearOfCertification();
        info.version = device.getVersion();
        info.fromExistingLink = true;
        device.getLifecycleDates().getReceivedDate().ifPresent((shipmentDate) -> info.shipmentTimeStamp = shipmentDate.toEpochMilli());
        return info;
    }

}
