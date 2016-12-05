package com.energyict.mdc.device.alarms.rest.response;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.rest.util.PropertyDescriptionInfo;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import org.osgi.service.component.annotations.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(name="device.alarm.info.factory", service = { InfoFactory.class }, immediate = true)
public class DeviceAlarmInfoFactory implements InfoFactory<DeviceAlarm> {

    private volatile DeviceService deviceService;

    public DeviceAlarmInfoFactory(){

    }

    @Inject
    public DeviceAlarmInfoFactory(DeviceService deviceService) {
        this();
        this.deviceService = deviceService;
    }


    @Override
    public Object from(DeviceAlarm deviceAlarm) {
        return asInfo(deviceAlarm, DeviceInfo.class);
    }

    @Override
    public List<PropertyDescriptionInfo> modelStructure() {
        return new ArrayList<>();
    }

    @Override
    public Class<DeviceAlarm> getDomainClass() {
        return DeviceAlarm.class;
    }

    public DeviceAlarmInfo<?> asInfo(DeviceAlarm deviceAlarm, Class<? extends DeviceInfo> deviceInfoClass) {
        DeviceAlarmInfo<?> info =  new DeviceAlarmInfo<>(deviceAlarm, deviceInfoClass);
        info.clearedStatus = deviceAlarm.getClearedStatus();
        addMeterInfo(info, deviceAlarm);
        return info;
    }

    private void addMeterInfo(DeviceAlarmInfo<?> info, DeviceAlarm deviceAlarm){
        if (deviceAlarm.getDevice() != null || deviceAlarm.getDevice().getAmrSystem().is(KnownAmrSystem.MDC)) {
            Optional<Device> deviceRef = deviceService.findDeviceById(Long.parseLong(deviceAlarm.getDevice().getAmrId()));
            if (deviceRef.isPresent()) {
                Device device = deviceRef.get();
                device.getUsagePoint().ifPresent(up -> info.usagePointMRID = up.getMRID());
                Optional<Location> location = device.getLocation();
                String formattedLocation = "";
                if (location.isPresent()) {
                    List<List<String>> formattedLocationMembers = location.get().format();
                    formattedLocationMembers.stream().skip(1).forEach(list ->
                            list.stream().filter(Objects::nonNull).findFirst().ifPresent(member -> list.set(list.indexOf(member), "\\r\\n" + member)));
                    formattedLocation = formattedLocationMembers.stream()
                            .flatMap(List::stream).filter(Objects::nonNull)
                            .collect(Collectors.joining(", "));
                }
                info.location = formattedLocation;
                info.deviceMRID = device.getmRID();
            }
        }
    }

}
