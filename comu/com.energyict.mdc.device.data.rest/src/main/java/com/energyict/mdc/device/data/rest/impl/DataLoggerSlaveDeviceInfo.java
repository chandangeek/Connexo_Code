package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.IntervalInfo;
import com.energyict.mdc.device.data.Device;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


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
    public String deviceConfigurationName;
    public String serialNumber;

    public List<DataLoggerSlaveChannelInfo> dataLoggerSlaveChannelInfos;   //mapping slave channel to data logger channel

    DataLoggerSlaveDeviceInfo(){
        super();
    }

    public boolean addDataLoggerSlaveChannelInfo(DataLoggerSlaveChannelInfo slaveChannelInfo){
        if (dataLoggerSlaveChannelInfos == null){
            dataLoggerSlaveChannelInfos = new ArrayList<>();
        }
        return dataLoggerSlaveChannelInfos.add(slaveChannelInfo);
    }

    // A DataLoggerSlaveDeviceInfo with only a list of {@link DataLoggerSlavechannelInfo}s is used
    // for keeping the data logger's channels which are not linked yet
    public boolean containsTheUnlinkedDataLoggerChannels(){
        return mRID == null;
    }

    static DataLoggerSlaveDeviceInfo from(Device device) {
        DataLoggerSlaveDeviceInfo info = new DataLoggerSlaveDeviceInfo();
        info.id = device.getId();
        info.mRID = device.getmRID();
        info.deviceTypeName = device.getDeviceType().getName();
        info.deviceConfigurationName = device.getDeviceConfiguration().getName();
        info.serialNumber = device.getSerialNumber();

        return info;
    }

}
