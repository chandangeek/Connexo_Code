/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;


@Component(name="com.energyict.mdc.device.config.validationruleset.deleted.eventhandler", service = TopicHandler.class, immediate = true)
public class ValidationRuleSetDeletedEventHandler implements TopicHandler {

    private volatile DeviceConfigurationService deviceConfigurationService;

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public String getTopicMatcher() {
        return "com/elster/jupiter/validation/validationruleset/DELETED";
    }

    @Override
    public void handle(LocalEvent localEvent) {
        ValidationRuleSet deletedRuleSet = (ValidationRuleSet)localEvent.getSource();
        List<DeviceConfiguration> deviceConfigurations = deviceConfigurationService.findDeviceConfigurationsForValidationRuleSet(deletedRuleSet.getId());
        for(DeviceConfiguration deviceConfiguration : deviceConfigurations) {
            deviceConfiguration.removeValidationRuleSet(deletedRuleSet);
        }
    }
}
