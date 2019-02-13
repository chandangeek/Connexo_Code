/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.impl.servicecall;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.PassiveCalendar;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.StatusInformationTask;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaign;
import com.energyict.mdc.tou.campaign.impl.MessageSeeds;
import com.energyict.mdc.tou.campaign.impl.TranslationKeys;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class TimeOfUseCampaignHandler extends EventHandler<LocalEvent> {

    private TimeOfUseCampaignServiceImpl timeOfUseCampaignService;
    private Clock clock;
    private ServiceCallService serviceCallService;

    @Inject
    public TimeOfUseCampaignHandler(TimeOfUseCampaignServiceImpl timeOfUseCampaignService, Clock clock, ServiceCallService serviceCallService) {
        super(LocalEvent.class);
        setTimeOfUseCampaignService(timeOfUseCampaignService);
        setServiceCallService(serviceCallService);
        setClock(clock);
    }

    @Override
    protected void onEvent(LocalEvent event, Object... eventDetails) {
        if (event.getType().getTopic().equals(EventType.MANUAL_COMTASKEXECUTION_STARTED.topic())
                || event.getType().getTopic().equals(EventType.SCHEDULED_COMTASKEXECUTION_STARTED.topic())) {
            processEvent(event, this::onComTaskStarted);
        } else if (event.getType().getTopic().equals(EventType.MANUAL_COMTASKEXECUTION_COMPLETED.topic())
                || event.getType().getTopic().equals(EventType.SCHEDULED_COMTASKEXECUTION_COMPLETED.topic())) {
            processEvent(event, this::onComTaskCompleted);
        } else if (event.getType().getTopic().equals(EventType.MANUAL_COMTASKEXECUTION_FAILED.topic())
                || event.getType().getTopic().equals(EventType.SCHEDULED_COMTASKEXECUTION_FAILED.topic())) {
            processEvent(event, this::onComTaskFailed);
        } else if (event.getType().getTopic().equals(com.energyict.mdc.tou.campaign.impl.EventType.TOU_CAMPAIGN_EDITED.topic())) {
            CompletableFuture.runAsync(() -> timeOfUseCampaignService.editCampaignItems((TimeOfUseCampaign) event.getSource()), Executors.newSingleThreadExecutor());
        }
    }

    private void onComTaskFailed(ComTaskExecution comTaskExecution) {
        if (forCalendar(comTaskExecution)) {
            if (timeOfUseCampaignService.getCampaignOn(comTaskExecution).isPresent()) {
                boolean planning = true;
                TimeOfUseCampaign timeOfUseCampaign = timeOfUseCampaignService.getCampaignOn(comTaskExecution).get();
                Device device = comTaskExecution.getDevice();
                if (plannedCalendarIsOnCampaign(device, timeOfUseCampaign)) {
                    if (device.calendars().getPlannedPassive()
                            .flatMap(PassiveCalendar::getDeviceMessage)
                            .map(DeviceMessage::getStatus)
                            .filter(deviceMessageStatus -> deviceMessageStatus.equals(DeviceMessageStatus.FAILED))
                            .isPresent()) {
                        ServiceCall serviceCall = timeOfUseCampaignService.findActiveServiceCallByDevice(device).get();
                        serviceCallService.lockServiceCall(serviceCall.getId());
                        serviceCall.requestTransition(DefaultState.FAILED);
                        timeOfUseCampaignService.logInServiceCall(serviceCall, MessageSeeds.CALENDAR_INSTALLATION_FAILED, LogLevel.INFO);
                        planning = false;
                    }
                }
                if (planning) {
                    if (device.getComTaskExecutions().stream()
                            .noneMatch(comTaskExecution1 -> comTaskExecution1.getNextExecutionTimestamp().equals(timeOfUseCampaign.getActivationStart()))) {
                        comTaskExecution.schedule(timeOfUseCampaign.getActivationStart());
                    }
                }
            }
        } else if (comTaskExecution.getComTask().getProtocolTasks().stream()
                .anyMatch(StatusInformationTask.class::isInstance)) {
            if (timeOfUseCampaignService.getCampaignOn(comTaskExecution).isPresent()) {
                TimeOfUseCampaign timeOfUseCampaign = timeOfUseCampaignService.getCampaignOn(comTaskExecution).get();
                if (withVerification(timeOfUseCampaign)) {
                    ServiceCall serviceCall = timeOfUseCampaignService.findActiveServiceCallByDevice(comTaskExecution.getDevice()).get();
                    if (serviceCall.getExtension(TimeOfUseItemDomainExtension.class)
                            .flatMap(TimeOfUseItemDomainExtension::getDeviceMessage)
                            .map(DeviceMessage::getStatus)
                            .filter(deviceMessageStatus -> deviceMessageStatus.equals(DeviceMessageStatus.CONFIRMED))
                            .isPresent()) {
                        serviceCallService.lockServiceCall(serviceCall.getId());
                        serviceCall.requestTransition(DefaultState.FAILED);
                        timeOfUseCampaignService.logInServiceCall(serviceCall, MessageSeeds.VERIFICATION_FAILED, LogLevel.INFO);
                    }
                }
            }
        }

    }

    private void onComTaskCompleted(ComTaskExecution comTaskExecution) {
        if (forCalendar(comTaskExecution)) {
            if (timeOfUseCampaignService.getCampaignOn(comTaskExecution).isPresent()) {
                boolean planning = true;
                TimeOfUseCampaign timeOfUseCampaign = timeOfUseCampaignService.getCampaignOn(comTaskExecution).get();
                Device device = comTaskExecution.getDevice();
                if (plannedCalendarIsOnCampaign(device, timeOfUseCampaign)) {
                    if (device.calendars().getPlannedPassive()
                            .flatMap(PassiveCalendar::getDeviceMessage)
                            .map(DeviceMessage::getStatus)
                            .filter(deviceMessageStatus -> deviceMessageStatus.equals(DeviceMessageStatus.CONFIRMED))
                            .isPresent()) {
                        ServiceCall serviceCall = timeOfUseCampaignService.findActiveServiceCallByDevice(comTaskExecution.getDevice()).get();
                        if (!withVerification(timeOfUseCampaign)) {
                            serviceCallService.lockServiceCall(serviceCall.getId());
                            serviceCall.requestTransition(DefaultState.SUCCESSFUL);
                            timeOfUseCampaignService.logInServiceCall(serviceCall, MessageSeeds.CALENDAR_INSTALLATION_COMPLETED, LogLevel.INFO);
                            timeOfUseCampaignService.revokeCalendarsCommands(device); //if calendar was installed out campaign
                            planning = false;
                        } else {
                            scheduleVerification(device, timeOfUseCampaign.getValidationTimeout());
                            timeOfUseCampaignService.logInServiceCall(serviceCall, MessageSeeds.VERIFICATION_SCHEDULED, LogLevel.INFO);
                            planning = false;
                        }
                    }
                }
                if (planning) {
                    if (device.getComTaskExecutions().stream()
                            .noneMatch(comTaskExecution1 -> comTaskExecution1.getNextExecutionTimestamp().equals(timeOfUseCampaign.getActivationStart()))) {
                        comTaskExecution.schedule(timeOfUseCampaign.getActivationStart());
                    }
                }
            }
        } else if (comTaskExecution.getComTask().getProtocolTasks().stream()
                .anyMatch(StatusInformationTask.class::isInstance)) {
            if (timeOfUseCampaignService.getCampaignOn(comTaskExecution).isPresent()) {
                TimeOfUseCampaign timeOfUseCampaign = timeOfUseCampaignService.getCampaignOn(comTaskExecution).get();
                if (withVerification(timeOfUseCampaign)) {
                    if (comTaskExecution.getDevice().calendars().getActive().isPresent()) {
                        ServiceCall serviceCall = timeOfUseCampaignService.findActiveServiceCallByDevice(comTaskExecution.getDevice()).get();
                        if (serviceCall.getExtension(TimeOfUseItemDomainExtension.class)
                                .flatMap(TimeOfUseItemDomainExtension::getDeviceMessage)
                                .map(DeviceMessage::getStatus)
                                .filter(deviceMessageStatus -> deviceMessageStatus.equals(DeviceMessageStatus.CONFIRMED))
                                .isPresent()) {
                            if (comTaskExecution.getDevice().calendars().getActive().get().getAllowedCalendar().getCalendar()
                                    .map(Calendar::getId)
                                    .filter(id -> id == timeOfUseCampaign.getCalendar().getId())
                                    .isPresent()) {
                                serviceCallService.lockServiceCall(serviceCall.getId());
                                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
                                timeOfUseCampaignService.logInServiceCall(serviceCall, MessageSeeds.VERIFICATION_COMPLETED, LogLevel.INFO);
                            } else {
                                serviceCallService.lockServiceCall(serviceCall.getId());
                                serviceCall.requestTransition(DefaultState.FAILED);
                                timeOfUseCampaignService.logInServiceCall(serviceCall, MessageSeeds.VERIFICATION_FAILED_WRONG_CALENDAR, LogLevel.INFO);
                            }
                        }
                    }
                }
            }
        }
    }

    private void onComTaskStarted(ComTaskExecution comTaskExecution) {
        if (forCalendar(comTaskExecution)) {
            if (timeOfUseCampaignService.getCampaignOn(comTaskExecution).isPresent()) {
                boolean planning = true;
                TimeOfUseCampaign timeOfUseCampaign = timeOfUseCampaignService.getCampaignOn(comTaskExecution).get();
                Device device = comTaskExecution.getDevice();
                if (shouldCalendarBeInstalled(device)) {
                    if (plannedCalendarIsOnCampaign(device, timeOfUseCampaign)) {
                        ServiceCall serviceCall = timeOfUseCampaignService.findActiveServiceCallByDevice(comTaskExecution.getDevice()).get();
                        serviceCallService.lockServiceCall(serviceCall.getId());
                        serviceCall.requestTransition(DefaultState.ONGOING);
                        timeOfUseCampaignService.logInServiceCall(serviceCall, MessageSeeds.CALENDAR_INSTALLATION_STARTED, LogLevel.INFO);
                        planning = false;
                    }
                } else if (comTaskExecution.getDevice().calendars().getPlannedPassive().isPresent()) {
                    if (comTaskExecution.getDevice().calendars().getPlannedPassive()
                            .flatMap(PassiveCalendar::getDeviceMessage)
                            .map(DeviceMessage::getStatus)
                            .filter(deviceMessageStatus -> deviceMessageStatus.equals(DeviceMessageStatus.CANCELED))
                            .isPresent()) {
                        timeOfUseCampaignService.cancelDevice(comTaskExecution.getDevice());
                        planning = false;
                    }
                } else {
                    timeOfUseCampaignService.cancelDevice(comTaskExecution.getDevice());
                    planning = false;
                }
                if (planning) {
                    if (device.getComTaskExecutions().stream()
                            .noneMatch(comTaskExecution1 -> comTaskExecution1.getNextExecutionTimestamp().equals(timeOfUseCampaign.getActivationStart()))) {
                        comTaskExecution.schedule(timeOfUseCampaign.getActivationStart());
                    }
                }
            }
        }

    }

    private boolean plannedCalendarIsOnCampaign(Device device, TimeOfUseCampaign timeOfUseCampaign) {
        return device.calendars().getPlannedPassive()
                .map(PassiveCalendar::getAllowedCalendar)
                .flatMap(AllowedCalendar::getCalendar)
                .map(Calendar::getId)
                .filter(id -> id == timeOfUseCampaign.getCalendar().getId())
                .isPresent();
    }

    private boolean shouldCalendarBeInstalled(Device device) {
        return device.getMessages().stream()
                .filter(deviceMessage -> (deviceMessage.getStatus().equals(DeviceMessageStatus.PENDING)
                        || deviceMessage.getStatus().equals(DeviceMessageStatus.WAITING)))
                .filter(deviceMessage -> deviceMessage.getSpecification().getCategory().getId() == 0)
                .anyMatch(deviceMessage -> !deviceMessage.getReleaseDate().isAfter(clock.instant()));
    }

    private interface EventProcessor {
        void process(ComTaskExecution source);
    }

    private void processEvent(LocalEvent event, EventProcessor processor) {
        Object source = event.getSource();
        if (source instanceof ComTaskExecution) {
            ComTaskExecution comTaskExecution = (ComTaskExecution) source;
            processor.process(comTaskExecution);
        }
    }

    private boolean forCalendar(ComTaskExecution comTaskExecution) {
        return comTaskExecution.getComTask().getProtocolTasks().stream()
                .filter(task -> task instanceof MessagesTask)
                .map(task -> ((MessagesTask) task))
                .map(MessagesTask::getDeviceMessageCategories)
                .flatMap(List::stream)
                .anyMatch(deviceMessageCategory -> deviceMessageCategory.getId() == 0);
    }

    private void scheduleVerification(Device device, long validationTimeout) {
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
            comTaskExecution.schedule(clock.instant().plusSeconds(validationTimeout));
        } else {
            ServiceCall serviceCall = timeOfUseCampaignService.findActiveServiceCallByDevice(device).get();
            serviceCallService.lockServiceCall(serviceCall.getId());
            timeOfUseCampaignService.logInServiceCall(serviceCall, MessageSeeds.ACTIVE_VERIFICATION_TASK_NOT_FOUND, LogLevel.SEVERE);
            serviceCall.requestTransition(DefaultState.FAILED);
        }
    }

    public enum EventType {
        MANUAL_COMTASKEXECUTION_STARTED("manualcomtaskexecution/STARTED"),
        MANUAL_COMTASKEXECUTION_COMPLETED("manualcomtaskexecution/COMPLETED"),
        MANUAL_COMTASKEXECUTION_FAILED("manualcomtaskexecution/FAILED"),
        SCHEDULED_COMTASKEXECUTION_STARTED("scheduledcomtaskexecution/STARTED"),
        SCHEDULED_COMTASKEXECUTION_COMPLETED("scheduledcomtaskexecution/COMPLETED"),
        SCHEDULED_COMTASKEXECUTION_FAILED("scheduledcomtaskexecution/FAILED");
        String topic;
        private static final String NAMESPACE = "com/energyict/mdc/device/data/";

        EventType(String topic) {
            this.topic = topic;
        }

        public String topic() {
            return NAMESPACE + topic;
        }

    }

    private boolean withVerification(TimeOfUseCampaign timeOfUseCampaign) {
        return timeOfUseCampaign.getActivationOption().equals(TranslationKeys.IMMEDIATELY.getKey());
    }

    @Reference
    public void setTimeOfUseCampaignService(TimeOfUseCampaignServiceImpl timeOfUseCampaignService) {
        this.timeOfUseCampaignService = timeOfUseCampaignService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }
}
