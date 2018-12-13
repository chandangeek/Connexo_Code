/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.configchange;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceConfigConflictMapping;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.ServerDeviceService;

import org.osgi.service.component.annotations.Reference;

public class ConflictCreationEventHandler implements TopicHandler {

    private static final String TOPIC = "com/energyict/mdc/device/config/deviceconfigconflict/VALIDATE_CREATE";
    private volatile DeviceService deviceService;
    private volatile Thesaurus thesaurus;

    public ConflictCreationEventHandler() {
    }

    @Override
    public void handle(LocalEvent localEvent) {
        final DeviceConfigConflictMapping deviceConfigConflictMapping = (DeviceConfigConflictMapping) localEvent.getSource();
        if(((ServerDeviceService) deviceService).hasActiveDeviceConfigChangesFor(deviceConfigConflictMapping.getOriginDeviceConfiguration(), deviceConfigConflictMapping.getDestinationDeviceConfiguration())){
            throw new VetoCreateNewConflictForActiveConfigChange(thesaurus);
        }
    }

    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService){
        this.deviceService = deviceService;
    }

    @Reference
    public void setThesaurus(Thesaurus thesaurus){
        this.thesaurus = thesaurus;
    }
}
