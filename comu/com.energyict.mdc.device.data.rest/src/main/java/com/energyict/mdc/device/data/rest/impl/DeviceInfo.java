package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.favorites.FavoritesService;
import com.elster.jupiter.favorites.DeviceLabel;
import com.elster.jupiter.favorites.LabelCategory;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.configuration.rest.GatewayTypeAdapter;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.imp.Batch;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.energyict.mdc.device.data.rest.FlaggedInfo;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
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
    public List<DeviceTopologyInfo> slaveDevices;
    public long nbrOfDataCollectionIssues;
    @XmlJavaTypeAdapter(GatewayTypeAdapter.class)
    public GatewayType gatewayType;
    public Boolean hasRegisters;
    public Boolean hasLogBooks;
    public Boolean hasLoadProfiles;
    public FlaggedInfo flaggedInfo;
    
    public DeviceInfo() {
    }

    public static DeviceInfo from(Device device, List<DeviceTopologyInfo> slaveDevices, DeviceImportService deviceImportService, DeviceService deviceService, IssueService issueService, MeteringService meteringService, FavoritesService favoritesService, User user) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.id = device.getId();
        deviceInfo.mRID = device.getmRID();
        deviceInfo.serialNumber = device.getSerialNumber();
        deviceInfo.deviceTypeId = device.getDeviceType().getId();
        deviceInfo.deviceTypeName = device.getDeviceType().getName();
        deviceInfo.deviceConfigurationId = device.getDeviceConfiguration().getId();
        deviceInfo.deviceConfigurationName = device.getDeviceConfiguration().getName();
        if (device.getYearOfCertification()!= null) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy").withZone(ZoneId.of("UTC"));
            deviceInfo.yearOfCertification = dateTimeFormatter.format(device.getYearOfCertification());
        }
        Optional<Batch> optionalBatch = deviceImportService.findBatch(device.getId());
        if (optionalBatch.isPresent()) {
            deviceInfo.batch = optionalBatch.get().getName();
        }
        if (device.getPhysicalGateway() != null) {
            deviceInfo.masterDeviceId = device.getPhysicalGateway().getId();
            deviceInfo.masterDevicemRID = device.getPhysicalGateway().getmRID();
        }

        deviceInfo.gatewayType = device.getConfigurationGatewayType();
        deviceInfo.slaveDevices = slaveDevices;
        deviceInfo.nbrOfDataCollectionIssues = issueService.countOpenDataCollectionIssues(device.getmRID());
        deviceInfo.hasLoadProfiles = !device.getLoadProfiles().isEmpty();
        deviceInfo.hasLogBooks = !device.getLogBooks().isEmpty();
        deviceInfo.hasRegisters = !device.getRegisters().isEmpty();
        deviceInfo.flaggedInfo = getFlaggedDeviceInfo(device, meteringService, favoritesService, user);
        
        return deviceInfo;
    }

    private static FlaggedInfo getFlaggedDeviceInfo(Device device, MeteringService meteringService, FavoritesService favoritesService, User user) {
        FlaggedInfo flaggedInfo = null;
        Optional<AmrSystem> amrSystem = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId());
        if (amrSystem.isPresent()) {
            Optional<Meter> meter = amrSystem.get().findMeter(String.valueOf(device.getId()));
            Optional<LabelCategory> favoriteCategory = favoritesService.findLabelCategory("mdc.labelcategory.favorite");
            if (meter.isPresent() && favoriteCategory.isPresent()) {
                Optional<DeviceLabel> deviceLabel = favoritesService.findDeviceLabel(meter.get(), user, favoriteCategory.get());
                if (deviceLabel.isPresent()) {
                    flaggedInfo = new FlaggedInfo();
                    flaggedInfo.status = true;
                    flaggedInfo.comment = deviceLabel.get().getComment();
                    flaggedInfo.flaggedDate = deviceLabel.get().getCreationDate();
                }
            }
        }
        return flaggedInfo;
    }

    public static DeviceInfo from(Device device) {
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
