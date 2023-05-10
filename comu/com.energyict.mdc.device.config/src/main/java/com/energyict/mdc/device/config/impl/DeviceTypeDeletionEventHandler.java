/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.EventType;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.exceptions.VetoDeleteDeviceTypeException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.energyict.mdc.device.config.devicetype.deleted.eventhandler", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class DeviceTypeDeletionEventHandler implements TopicHandler {

    private volatile MeteringGroupsService meteringGroupsService;
    private volatile Thesaurus thesaurus;

    public DeviceTypeDeletionEventHandler() {
    }

    @Inject
    public DeviceTypeDeletionEventHandler(MeteringGroupsService meteringGroupsService, Thesaurus thesaurus) {
        this();
        this.thesaurus = thesaurus;
        setMeteringGroupsService(meteringGroupsService);
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceConfigurationService.COMPONENTNAME, Layer.SERVICE);
    }


    @Override
    public void handle(LocalEvent localEvent) {
        this.validateDelete((DeviceType) localEvent.getSource());
    }

    @Override
    public String getTopicMatcher() {
        return EventType.DEVICETYPE_VALIDATE_DELETE.topic();
    }

    private void validateDelete(DeviceType deviceType){
        this.meteringGroupsService.getQueryEndDeviceGroupQuery().select(Condition.TRUE).stream()
                .flatMap(deviceGroup -> deviceGroup.getSearchablePropertyValues().stream())
                .filter(searchablePropertyValue ->
                        DeviceConfigurationImpl.Fields.DEVICETYPE.fieldName().equals(searchablePropertyValue.getProperty().getName()))
                .flatMap(searchablePropertyValue -> searchablePropertyValue.getValues().stream())
                .filter(deviceType::equals)
                .findAny()
                .ifPresent(o -> {
                    throw new VetoDeleteDeviceTypeException(this.thesaurus, deviceType);
                });
    }
}
