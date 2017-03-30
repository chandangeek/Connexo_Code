/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.scheduling.events.EventType;
import com.energyict.mdc.scheduling.model.ComSchedule;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Handles events that are being sent when a {@link ComSchedule} is about to be
 * made obsolete and will veto that when it is in use by at least one device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-03 (14:27)
 */
@Component(name = "com.energyict.mdc.device.data.comschedule.obsolete.validator", service = TopicHandler.class, immediate = true)
public class ComScheduleObsoleteValidator implements TopicHandler {

    static final String TOPIC = EventType.COMSCHEDULES_BEFORE_OBSOLETE.topic();

    private volatile DeviceDataModelService deviceDataModelService;
    private volatile Thesaurus thesaurus;

    public ComScheduleObsoleteValidator() {
        super();
    }

    // For testing purposes only
    ComScheduleObsoleteValidator(DeviceDataModelService deviceDataModelService) {
        this();
        this.setDeviceDataModelService(deviceDataModelService);
    }

    @Reference
    public void setDeviceDataModelService(DeviceDataModelService deviceDataModelService) {
        this.deviceDataModelService = deviceDataModelService;
        this.thesaurus = deviceDataModelService.thesaurus();
    }

    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }

    @Override
    public void handle(LocalEvent event) {
        ComSchedule comSchedule = (ComSchedule) event.getSource();
        this.validateNotUsedByDevice(comSchedule);
    }

    /**
     * Vetos the obsoletion of the {@link ComSchedule}
     * by throwing an exception when the ComSchedule
     * is used by at least on Device, i.e. at least one
     * ComTaskExecutions
     * for that ComSchedule was created on that Device.
     *
     * @param comSchedule The ComTaskEnablement that is about to be deleted
     */
    private void validateNotUsedByDevice(ComSchedule comSchedule) {
        if (this.deviceDataModelService.communicationTaskService().hasComTaskExecutions(comSchedule)) {
            throw new VetoObsoleteComScheduleException(this.thesaurus, comSchedule);
        }
    }

}