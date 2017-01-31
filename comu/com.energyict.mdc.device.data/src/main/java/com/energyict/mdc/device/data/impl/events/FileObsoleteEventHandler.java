/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.config.DeviceMessageFile;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.events.EventType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.energyict.mdc.device.data.obsolete.file.eventhandler", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class FileObsoleteEventHandler implements TopicHandler {

    private static final String TOPIC = EventType.DEVICE_MESSAGE_FILE_OBSOLETE.topic();

    private volatile DeviceDataModelService deviceDataModelService;
    private Thesaurus thesaurus;

    @Reference
    public void setDeviceDataModelService(DeviceDataModelService deviceDataModelService) {
        this.deviceDataModelService = deviceDataModelService;
        this.thesaurus = deviceDataModelService.thesaurus();
    }

    @Override
    public void handle(LocalEvent localEvent) {
        DeviceMessageFile deviceMessageFile = (DeviceMessageFile) localEvent.getSource();
        DeviceType deviceType = deviceMessageFile.getDeviceType();
        DeviceMessageSpecificationService deviceMessageSpecificationService = deviceDataModelService.deviceMessageSpecificationService();
        List<String> propertyNames = deviceMessageSpecificationService.allCategories().stream()
                .map(DeviceMessageCategory::getMessageSpecifications)
                .flatMap(Collection::stream)
                .map(DeviceMessageSpec::getPropertySpecs)
                .flatMap(Collection::stream)
                .filter(propertySpec -> propertySpec.isReference() && propertySpec.getValueFactory().getValueType().isAssignableFrom(com.energyict.mdc.protocol.api.DeviceMessageFile.class))
                .map(PropertySpec::getName)
                .collect(Collectors.toList());


        DataModel dataModel = deviceDataModelService.dataModel();
        List<DeviceMessage> deviceMessages = dataModel.query(DeviceMessage.class, DeviceMessageAttribute.class, Device.class)
                .select(where("deviceMessageAttributes.stringValue").isEqualTo(String.valueOf(deviceMessageFile.getId()))
                        .and(where("device.deviceType").isEqualTo(deviceType))
                        .and(where("deviceMessageAttributes.name").in(propertyNames)));
        deviceMessages.stream()
                .forEach(DeviceMessage::revoke);
    }

    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }
}
