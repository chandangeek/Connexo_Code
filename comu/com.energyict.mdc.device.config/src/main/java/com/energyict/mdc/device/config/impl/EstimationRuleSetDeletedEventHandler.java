/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

@Component(name = "com.energyict.mdc.device.config.estimationruleset.deleted.eventhandler", service = TopicHandler.class, immediate = true)
public class EstimationRuleSetDeletedEventHandler implements TopicHandler {

    private volatile DeviceConfigurationService deviceConfigurationService;

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public String getTopicMatcher() {
        return "com/elster/jupiter/estimation/estimationruleset/DELETED";
    }

    @Override
    public void handle(LocalEvent localEvent) {
        EstimationRuleSet deletedRuleSet = (EstimationRuleSet) localEvent.getSource();
        List<DeviceConfiguration> deviceConfigurations = deviceConfigurationService.findDeviceConfigurationsForEstimationRuleSet(deletedRuleSet).find();
        for (DeviceConfiguration deviceConfiguration : deviceConfigurations) {
            deviceConfiguration.removeEstimationRuleSet(deletedRuleSet);
        }
    }
}
