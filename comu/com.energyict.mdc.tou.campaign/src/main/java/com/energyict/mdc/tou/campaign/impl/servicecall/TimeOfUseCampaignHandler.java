/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.impl.servicecall;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.StatusInformationTask;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaign;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Optional;

@Component(name = "com.energyict.mdc.tou.campaign.handler", service = Subscriber.class, immediate = true)
public class TimeOfUseCampaignHandler extends EventHandler<LocalEvent> {

    private TimeOfUseCampaignServiceImpl timeOfUseCampaignService;
    private Clock clock;

    @Inject
    public TimeOfUseCampaignHandler(TimeOfUseCampaignServiceImpl timeOfUseCampaignService, Clock clock) {
        super(LocalEvent.class);
        this.timeOfUseCampaignService = timeOfUseCampaignService;
        this.clock = clock;
    }

    public TimeOfUseCampaignHandler() {
        super(LocalEvent.class);
    }

    protected TimeOfUseCampaignHandler(Class<LocalEvent> eventType) {
        super(eventType);
    }

    @Override
    protected void onEvent(LocalEvent event, Object... eventDetails) {
        if (event.getType().getTopic().equals(EventType.TOU_COMTASKEXECUTION_STARTED.topic())) {
            boolean planing = false;
            ComTaskExecution comTaskExecution = (ComTaskExecution) event.getSource();
            if (timeOfUseCampaignService.getCampaignOn(comTaskExecution).isPresent()) {
                planing = true;
                if (comTaskExecution.getDevice().getMessages().stream()
                        .filter(deviceMessage -> (deviceMessage.getStatus().equals(DeviceMessageStatus.PENDING)
                                || deviceMessage.getStatus().equals(DeviceMessageStatus.WAITING)))
                        .filter(deviceMessage -> deviceMessage.getSpecification().getCategory().getId() == 0)
                        .anyMatch(deviceMessage -> !deviceMessage.getReleaseDate().isAfter(clock.instant()))) {
                    if (comTaskExecution.getDevice().calendars().getPlannedPassive().isPresent()) {
                        if (timeOfUseCampaignService.getCampaignOn(comTaskExecution).isPresent()) {
                            if (comTaskExecution.getDevice().calendars().getPlannedPassive().get().getAllowedCalendar().getId()
                                    == timeOfUseCampaignService.getCampaignOn(comTaskExecution).get().getCalendar().getId()) {
                                timeOfUseCampaignService.changeServiceCallStatus(comTaskExecution.getDevice().getName(), DefaultState.ONGOING);
                                timeOfUseCampaignService.logInServiceCallByDevice(comTaskExecution.getDevice(), EventType.TOU_COMTASKEXECUTION_STARTED.topic, LogLevel.INFO);
                                planing = false;
                            }
                        }
                    }
                } else if (comTaskExecution.getDevice().calendars().getPlannedPassive().isPresent()) {
                    if (comTaskExecution.getDevice().calendars().getPlannedPassive().get().getDeviceMessage().isPresent()) {
                        if (comTaskExecution.getDevice().calendars().getPlannedPassive().get().getDeviceMessage().get().getStatus().equals(DeviceMessageStatus.CANCELED)) {
                            timeOfUseCampaignService.cancelDevice(comTaskExecution.getDevice().getName());
                            planing = false;
                        }
                    }
                } else {
                    timeOfUseCampaignService.cancelDevice(comTaskExecution.getDevice().getName());
                    planing = false;
                }
            }
            if (planing) {
                comTaskExecution.schedule(timeOfUseCampaignService.getCampaignOn(comTaskExecution).get().getActivationStart());
            }
        } else if (event.getType().getTopic().equals(EventType.TOU_COMTASKEXECUTION_COMPLETED.topic())) {
            boolean planing = false;
            ComTaskExecution comTaskExecution = (ComTaskExecution) event.getSource();
            if (timeOfUseCampaignService.getCampaignOn(comTaskExecution).isPresent()) {
                planing = true;
                if (comTaskExecution.getDevice().calendars().getPlannedPassive().isPresent()) {
                    if (comTaskExecution.getDevice().calendars().getPlannedPassive().get().getAllowedCalendar().getId()
                            == timeOfUseCampaignService.getCampaignOn(comTaskExecution).get().getCalendar().getId()) {
                        if (comTaskExecution.getDevice().calendars().getPlannedPassive().get().getDeviceMessage().get().getStatus().equals(DeviceMessageStatus.CONFIRMED)) {
                            if (!withVerification(comTaskExecution)) {
                                timeOfUseCampaignService.changeServiceCallStatus(comTaskExecution.getDevice().getName(), DefaultState.SUCCESSFUL);
                                timeOfUseCampaignService.logInServiceCallByDevice(comTaskExecution.getDevice(), EventType.TOU_COMTASKEXECUTION_COMPLETED.topic, LogLevel.INFO);
                                timeOfUseCampaignService.revokeCalendarsCommands(comTaskExecution.getDevice());
                                planing = false;
                            } else {
                                timeOfUseCampaignService.getCampaignOn(comTaskExecution)
                                        .ifPresent(timeOfUseCampaign1 -> scheduleVerification(comTaskExecution.getDevice(), timeOfUseCampaign1.getTimeValidation()));
                                timeOfUseCampaignService.logInServiceCallByDevice(comTaskExecution.getDevice(), "verification scheduled", LogLevel.INFO);
                                planing = false;
                            }
                        }
                    }
                }
            }
            if (planing) {
                comTaskExecution.schedule(timeOfUseCampaignService.getCampaignOn(comTaskExecution).get().getActivationStart());
            }
        } else if (event.getType().getTopic().equals(EventType.TOU_COMTASKEXECUTION_FAILED.topic())) {
            boolean planing = false;
            ComTaskExecution comTaskExecution = (ComTaskExecution) event.getSource();
            if (timeOfUseCampaignService.getCampaignOn(comTaskExecution).isPresent()) {
                planing = true;
                if (comTaskExecution.getDevice().calendars().getPlannedPassive().isPresent()) {
                    if (comTaskExecution.getDevice().calendars().getPlannedPassive().get().getAllowedCalendar().getId()
                            == timeOfUseCampaignService.getCampaignOn(comTaskExecution).get().getCalendar().getId()) {
                        if (comTaskExecution.getDevice().calendars().getPlannedPassive().get().getDeviceMessage().get().getStatus().equals(DeviceMessageStatus.FAILED)) {
                            timeOfUseCampaignService.changeServiceCallStatus(comTaskExecution.getDevice().getName(), DefaultState.FAILED);
                            timeOfUseCampaignService.logInServiceCallByDevice(comTaskExecution.getDevice(), EventType.TOU_COMTASKEXECUTION_FAILED.topic, LogLevel.INFO);
                            planing = false;
                        }
                    }
                }
            }
            if (planing) {
                comTaskExecution.schedule(timeOfUseCampaignService.getCampaignOn(comTaskExecution).get().getActivationStart());
            }
        } else if (event.getType().getTopic().equals(EventType.VERIFICATION_FAILED.topic())) {
            ComTaskExecution comTaskExecution = (ComTaskExecution) event.getSource();
            if (timeOfUseCampaignService.getCampaignOn(comTaskExecution).isPresent()) {
                if (withVerification(comTaskExecution)) {
                    if (comTaskExecution.getDevice().getMessages().stream()
                            .filter(deviceMessage -> deviceMessage.getStatus().equals(DeviceMessageStatus.CONFIRMED))
                            .filter(deviceMessage -> deviceMessage.getSpecification().getCategory().getId() == 0)
                            .anyMatch(deviceMessage -> deviceMessage.getReleaseDate().equals(timeOfUseCampaignService.getCampaignOn(comTaskExecution).get().getActivationStart()))) {
                        if (withVerification(comTaskExecution)) {
                            timeOfUseCampaignService.changeServiceCallStatus(comTaskExecution.getDevice().getName(), DefaultState.FAILED);
                            timeOfUseCampaignService.logInServiceCallByDevice(comTaskExecution.getDevice(), EventType.VERIFICATION_FAILED.topic, LogLevel.INFO);
                        }
                    }
                }
            }
        } else if (event.getType().getTopic().equals(EventType.VERIFICATION_COMPLETED.topic())) {
            ComTaskExecution comTaskExecution = (ComTaskExecution) event.getSource();
            if (timeOfUseCampaignService.getCampaignOn(comTaskExecution).isPresent()) {
                if (withVerification(comTaskExecution)) {
                    if (comTaskExecution.getDevice().calendars().getActive().isPresent()) {
                        if (comTaskExecution.getDevice().getMessages().stream()
                                .filter(deviceMessage -> (deviceMessage.getStatus().equals(DeviceMessageStatus.PENDING)
                                        || deviceMessage.getStatus().equals(DeviceMessageStatus.WAITING)))
                                .filter(deviceMessage -> deviceMessage.getSpecification().getCategory().getId() == 0)
                                .noneMatch(deviceMessage -> deviceMessage.getReleaseDate()
                                        .equals(timeOfUseCampaignService.getCampaignOn(comTaskExecution).get().getActivationStart()))) {
                            if (comTaskExecution.getDevice().calendars().getActive().get().getAllowedCalendar().getId()
                                    == timeOfUseCampaignService.getCampaignOn(comTaskExecution).get().getCalendar().getId()) {
                                timeOfUseCampaignService.changeServiceCallStatus(comTaskExecution.getDevice().getName(), DefaultState.SUCCESSFUL);
                                timeOfUseCampaignService.logInServiceCallByDevice(comTaskExecution.getDevice(), EventType.VERIFICATION_COMPLETED.topic, LogLevel.INFO);
                            } else {
                                timeOfUseCampaignService.changeServiceCallStatus(comTaskExecution.getDevice().getName(), DefaultState.FAILED);
                                timeOfUseCampaignService.logInServiceCallByDevice(comTaskExecution.getDevice(), "verification failed: wrong calendar", LogLevel.INFO);
                            }
                        }
                    }
                }
            }
        }
    }

    private void scheduleVerification(Device device, long timeValidation) {
        Optional<ComTaskEnablement> comTaskEnablementOptional = device.getDeviceConfiguration().getComTaskEnablements().stream()
                .filter(comTaskEnablement -> comTaskEnablement.getComTask().getProtocolTasks().stream()
                        .anyMatch(task -> task instanceof StatusInformationTask))
                .filter(comTaskEnablement -> !comTaskEnablement.isSuspended())
                .filter(comTaskEnablement -> comTaskEnablement.getComTask().getProtocolTasks().stream()
                        .noneMatch(protocolTask -> protocolTask instanceof MessagesTask))
                .filter(comTaskEnablement -> (timeOfUseCampaignService.findComTaskExecution(device, comTaskEnablement) == null)
                        || (!timeOfUseCampaignService.findComTaskExecution(device, comTaskEnablement).isOnHold()))
                .findAny();
        if (comTaskEnablementOptional.isPresent()) {
            ComTaskExecution comTaskExecution = device.getComTaskExecutions().stream()
                    .filter(comTaskExecution1 -> comTaskExecution1.getComTask().equals(comTaskEnablementOptional.get().getComTask()))
                    .findAny().orElse(null);
            if (comTaskExecution == null) {
                comTaskExecution = device.newAdHocComTaskExecution(comTaskEnablementOptional.get()).add();
            }
            comTaskExecution.schedule(clock.instant().plusSeconds(timeValidation));
        } else {
            timeOfUseCampaignService.logInServiceCallByDevice(device, "not found active verification task", LogLevel.SEVERE);
            timeOfUseCampaignService.changeServiceCallStatus(device.getName(), DefaultState.FAILED);
        }
    }

    public enum EventType {

        VERIFICATION_STARTED("verification/STARTED"),
        VERIFICATION_COMPLETED("verification/COMPLETED"),
        VERIFICATION_FAILED("verification/FAILED"),
        TOU_COMTASKEXECUTION_STARTED("toucomtaskexecution/STARTED"),
        TOU_COMTASKEXECUTION_COMPLETED("toucomtaskexecution/COMPLETED"),
        TOU_COMTASKEXECUTION_FAILED("toucomtaskexecution/FAILED");
        String topic;
        private static final String NAMESPACE = "com/energyict/mdc/device/data/";

        EventType(String topic) {
            this.topic = topic;
        }

        public String topic() {
            return NAMESPACE + topic;
        }
    }

    private boolean withVerification(ComTaskExecution comTaskExecution) {
        Optional<TimeOfUseCampaign> timeOfUseCampaign = timeOfUseCampaignService.getCampaignOn(comTaskExecution);
        if (timeOfUseCampaign.isPresent()) {
            if (timeOfUseCampaign.get().getActivationDate() != null) {
                return timeOfUseCampaign.get().getActivationDate().equals("Immediately");
            }
        }
        return false;
    }

    @Reference
    public void setTimeOfUseCampaignService(TimeOfUseCampaignServiceImpl timeOfUseCampaignService) {
        this.timeOfUseCampaignService = timeOfUseCampaignService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }
}
