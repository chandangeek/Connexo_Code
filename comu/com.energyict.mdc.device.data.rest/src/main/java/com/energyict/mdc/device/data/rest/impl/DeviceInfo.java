package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.imp.Batch;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.energyict.mdc.protocol.api.device.BaseDevice;

import com.elster.jupiter.issue.share.service.IssueService;
import com.google.common.base.Optional;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class DeviceInfo {

    @JsonIgnore
    static final String DEVICETYPE_NAME = "deviceTypeName";
    @JsonIgnore
    static final String DEVICE_CONFIGURATION_NAME = "deviceConfigurationName";
    @JsonIgnore
    static final String MASTER_DEVICE_MRID = "masterDevicemRID";

    public long id;
    public String mRID;
    public String serialNumber;
    @JsonProperty(DEVICETYPE_NAME)
    public String deviceTypeName;
    public Long deviceTypeId;
    @JsonProperty(DEVICE_CONFIGURATION_NAME)
    public String deviceConfigurationName;
    public Long deviceConfigurationId;
    public String yearOfCertification;
    public String batch;
    @JsonProperty(MASTER_DEVICE_MRID)
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
            deviceInfo.batch = device.getName();
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

    public static List<DeviceInfo> from(List<Device> devices, DeviceImportService deviceImportService, IssueService issueService) {
        List<DeviceInfo> deviceInfos = new ArrayList<>();
        for (Device device : devices) {
            deviceInfos.add(DeviceInfo.from(device, deviceImportService, issueService));
        }
        return deviceInfos;
    }


}
