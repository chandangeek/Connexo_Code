/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest.response;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.rest.util.PropertyDescriptionInfo;
import com.elster.jupiter.search.rest.InfoFactoryService;
import com.energyict.mdc.common.device.data.LogBook;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.event.DeviceAlarmRelatedEvent;
import com.energyict.mdc.device.data.LogBookService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(name = "device.alarm.info.factory", service = {InfoFactory.class}, immediate = true)
public class DeviceAlarmInfoFactory implements InfoFactory<DeviceAlarm> {

    private volatile LogBookService logBookService;

    public DeviceAlarmInfoFactory() {
        // For OSGi
    }

    @Inject
    public DeviceAlarmInfoFactory(LogBookService logBookService) {
        this();
        this.logBookService = logBookService;
    }

    @Reference
    public void setLogBookService(LogBookService logBookService) {
        this.logBookService = logBookService;
    }

    @Reference
    public void setInfoFactoryService(InfoFactoryService infoFactoryService) {
        // to make sure this factory starts after the whole service
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

    public DeviceAlarmInfo asInfo(DeviceAlarm deviceAlarm, Class<? extends DeviceInfo> deviceInfoClass) {
        DeviceAlarmInfo info = new DeviceAlarmInfo(deviceAlarm, deviceInfoClass);
        addLogBookInfo(info, deviceAlarm);
        addMeterInfo(info, deviceAlarm);
        addRelatedEvents(info, deviceAlarm);
        return info;
    }

    private void addMeterInfo(DeviceAlarmInfo info, DeviceAlarm deviceAlarm) {
        if (deviceAlarm.getDevice() != null || deviceAlarm.getDevice().getAmrSystem().is(KnownAmrSystem.MDC)) {
            info.device = new DeviceInfo(deviceAlarm.getDevice());
        }
    }

    private void addLogBookInfo(DeviceAlarmInfo info, DeviceAlarm deviceAlarm) {
        EndDeviceEventRecord currentEvent = Collections.max(deviceAlarm.getDeviceAlarmRelatedEvents()
                .stream()
                .map(DeviceAlarmRelatedEvent::getEventRecord)
                .collect(Collectors.toList()), Comparator
                .comparing(EndDeviceEventRecord::getCreateTime));
        Optional<LogBook> logBook = logBookService.findById(currentEvent.getLogBookId());
        if (logBook.isPresent()) {
            info.logBook = new IdWithNameInfo(logBook.get().getId(), logBook.get().getLogBookType().getName());
        }
    }

    private void addRelatedEvents(DeviceAlarmInfo info, DeviceAlarm deviceAlarm) {
        List<EndDeviceEventRecord> relatedEvents = deviceAlarm.getDeviceAlarmRelatedEvents()
                .stream()
                .map(DeviceAlarmRelatedEvent::getEventRecord)
                .collect(Collectors.toList());
        info.relatedEvents = relatedEvents.stream().map(RelatedEventsInfo::new).sorted((r1, r2) -> Long.compare(r2.eventDate, r1.eventDate)).collect(Collectors.toList());
    }

}
