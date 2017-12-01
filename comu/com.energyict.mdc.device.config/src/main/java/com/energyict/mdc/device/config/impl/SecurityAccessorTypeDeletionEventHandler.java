/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.energyict.mdc.device.config.DeviceType;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

/**
 * Listens to 'validate_delete' events of {@link com.elster.jupiter.pki.SecurityAccessorType}
 * and will veto the deletion if it is still referenced by a {@link com.energyict.mdc.device.config.DeviceType}
 */
@Component(name="com.energyict.mdc.device.config.impl.SecurityAccessorTypeDeletionEventHandler", service = TopicHandler.class, immediate = true)
public class SecurityAccessorTypeDeletionEventHandler implements TopicHandler {
    private volatile ServerDeviceConfigurationService deviceConfigurationService;

    // OSGi
    public SecurityAccessorTypeDeletionEventHandler() {
        super();
    }

    // For testing purposes only
    public SecurityAccessorTypeDeletionEventHandler(ServerDeviceConfigurationService deviceConfigurationService) {
        this();
        setDeviceConfigurationService(deviceConfigurationService);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        SecurityAccessorType source = (SecurityAccessorType) localEvent.getSource();
        List<DeviceType> blockingDeviceTypes = deviceConfigurationService.findDeviceTypesUsingSecurityAccessorType(source);
        if (!blockingDeviceTypes.isEmpty()) {
            throw new VetoDeleteSecurityAccessorTypeException(getThesaurus(), blockingDeviceTypes);
        }
    }

    private Thesaurus getThesaurus() {
        return deviceConfigurationService.getThesaurus();
    }

    @Override
    public String getTopicMatcher() {
        return "com/elster/jupiter/pki/securityAccessorType/VALIDATE_DELETE";
    }

    @Reference
    public void setDeviceConfigurationService(ServerDeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }
}
