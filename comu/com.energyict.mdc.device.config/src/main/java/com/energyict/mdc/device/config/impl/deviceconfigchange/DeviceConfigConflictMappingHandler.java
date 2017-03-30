/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl.deviceconfigchange;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.pubsub.Subscriber;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.events.PartialConnectionTaskUpdateDetails;

import org.osgi.service.component.annotations.Component;

import javax.inject.Inject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles all changes related to updates on a DeviceConfiguration which can lead to a recalculation of
 * the DeviceConfigConflictMappings of the DeviceType
 */
@Component(name = "com.energyict.mdc.device.data.impl.events.DeviceConfigConflictMappingHandler", service = Subscriber.class, immediate = true)
public class DeviceConfigConflictMappingHandler extends EventHandler<LocalEvent> {

    static final Pattern CONNECTIONTASK_TOPICS = Pattern.compile("com/energyict/mdc/device/config/partial(.*)connectiontask/(DELETED|CREATED)");
    static final Pattern CONNECTIONTASK_UPDATE_TOPICS = Pattern.compile("com/energyict/mdc/device/config/partial(.*)connectiontask/UPDATED");
    static final Pattern SECURITYSET_TOPICS = Pattern.compile("com/energyict/mdc/device/config/securitypropertyset/(DELETED|CREATED|UPDATED)");
    static final Pattern DEVICECONFIGURATION_TOPICS = Pattern.compile("com/energyict/mdc/device/config/deviceconfiguration/(DEACTIVATED|ACTIVATED)");

    @Inject
    public DeviceConfigConflictMappingHandler() {
        super(LocalEvent.class);
    }

    @Override
    public void onEvent(LocalEvent event, Object... eventDetails) {
        String topic = event.getType().getTopic();
        Matcher connectionTaskMatcher = CONNECTIONTASK_TOPICS.matcher(topic);
        Matcher connectionTaskUpdateMatcher = CONNECTIONTASK_UPDATE_TOPICS.matcher(topic);
        Matcher securitySetMatcher = SECURITYSET_TOPICS.matcher(topic);
        Matcher deviceConfigurationMatcher = DEVICECONFIGURATION_TOPICS.matcher(topic);
        if (connectionTaskMatcher.matches()) {
            PartialConnectionTask partialConnectionTask = (PartialConnectionTask) event.getSource();
            DeviceConfigConflictMappingEngine.INSTANCE.reCalculateConflicts(partialConnectionTask.getConfiguration().getDeviceType());
        } else if(connectionTaskUpdateMatcher.matches()){
            PartialConnectionTaskUpdateDetails partialConnectionTaskUpdateDetails = (PartialConnectionTaskUpdateDetails) event.getSource();
            DeviceConfigConflictMappingEngine.INSTANCE.reCalculateConflicts(partialConnectionTaskUpdateDetails.getPartialConnectionTask().getConfiguration().getDeviceType());
        } else if (securitySetMatcher.matches()) {
            SecurityPropertySet securityPropertySet = (SecurityPropertySet) event.getSource();
            DeviceConfigConflictMappingEngine.INSTANCE.reCalculateConflicts(securityPropertySet.getDeviceConfiguration().getDeviceType());
        } else if (deviceConfigurationMatcher.matches()) {
            DeviceConfiguration deviceConfiguration = (DeviceConfiguration) event.getSource();
            DeviceConfigConflictMappingEngine.INSTANCE.reCalculateConflicts(deviceConfiguration.getDeviceType());
        }
    }
}
