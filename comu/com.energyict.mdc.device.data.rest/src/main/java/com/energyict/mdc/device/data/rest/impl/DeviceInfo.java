package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.issue.share.service.IssueService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.imp.Batch;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.google.common.base.Optional;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DeviceInfo {

    public long id;
    public String mRID;
    public String serialNumber;
    public String deviceTypeName;
    public Long deviceTypeId;
    public String deviceConfigurationName;
    public Long deviceConfigurationId;
    public String yearOfCertification;
    public String batch;
    public String masterDevicemRID;
    public Long masterDeviceId;
    public List<DeviceInfo> slaveDevices;
    public long nbrOfDataCollectionIssues;

    public DeviceInfo() {
    }

    public static DeviceInfo from(Device device, DeviceImportService deviceImportService, IssueService issueService) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.id = device.getId();
        deviceInfo.mRID = device.getmRID();
        deviceInfo.serialNumber = device.getSerialNumber();
        deviceInfo.deviceTypeId = device.getDeviceType().getId();
        deviceInfo.deviceTypeName = device.getDeviceType().getName();
        deviceInfo.deviceConfigurationId = device.getDeviceConfiguration().getId();
        deviceInfo.deviceConfigurationName = device.getDeviceConfiguration().getName();
        if (device.getYearOfCertification()!= null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
            deviceInfo.yearOfCertification = dateFormat.format(device.getYearOfCertification());
        }
        Optional<Batch> optionalBatch = deviceImportService.findBatch(device.getId());
        if (optionalBatch.isPresent()) {
            deviceInfo.batch = optionalBatch.get().getName();
        }
        if (device.getPhysicalGateway() != null) {
            deviceInfo.masterDeviceId = device.getPhysicalGateway().getId();
            deviceInfo.masterDevicemRID = device.getPhysicalGateway().getmRID();
        }
        List<Device> slaves = device.getPhysicalConnectedDevices();
        deviceInfo.slaveDevices = new ArrayList<>();
        for (BaseDevice dev : slaves) {
            DeviceInfo slaveInfo = new DeviceInfo();
            slaveInfo.id  = dev.getId();
            slaveInfo.mRID = ((Device)dev).getmRID();
            deviceInfo.slaveDevices.add(slaveInfo);
        }
        deviceInfo.nbrOfDataCollectionIssues = issueService.countOpenDataCollectionIssues(device.getmRID());
        return deviceInfo;
    }

    private static DeviceInfo from(Device device) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.id = device.getId();
        deviceInfo.mRID = device.getmRID();
        deviceInfo.serialNumber = device.getSerialNumber();
        deviceInfo.deviceTypeId = device.getDeviceType().getId();
        deviceInfo.deviceTypeName = device.getDeviceType().getName();
        deviceInfo.deviceConfigurationId = device.getDeviceConfiguration().getId();
        deviceInfo.deviceConfigurationName = device.getDeviceConfiguration().getName();
        return deviceInfo;
    }

    public static List<DeviceInfo> from(List<Device> devices) {
        List<DeviceInfo> deviceInfos = new ArrayList<>();
        for (Device device : devices) {
            deviceInfos.add(DeviceInfo.from(device));
        }
        return deviceInfos;
    }

}
