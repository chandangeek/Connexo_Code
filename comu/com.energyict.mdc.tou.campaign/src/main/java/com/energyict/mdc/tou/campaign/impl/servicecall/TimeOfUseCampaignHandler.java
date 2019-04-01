/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.impl.servicecall;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.nls.Thesaurus;
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
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignItem;
import com.energyict.mdc.tou.campaign.impl.MessageSeeds;
import com.energyict.mdc.tou.campaign.impl.TranslationKeys;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import javax.inject.Inject;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class TimeOfUseCampaignHandler extends EventHandler<LocalEvent> {

    private final static String MANUAL_COMTASKEXECUTION_STARTED = "com/energyict/mdc/device/data/manualcomtaskexecution/STARTED";
    private final static String MANUAL_COMTASKEXECUTION_COMPLETED = "com/energyict/mdc/device/data/manualcomtaskexecution/COMPLETED";
    private final static String MANUAL_COMTASKEXECUTION_FAILED = "com/energyict/mdc/device/data/manualcomtaskexecution/FAILED";
    private final static String SCHEDULED_COMTASKEXECUTION_STARTED = "com/energyict/mdc/device/data/scheduledcomtaskexecution/STARTED";
    private final static String SCHEDULED_COMTASKEXECUTION_COMPLETED = "com/energyict/mdc/device/data/scheduledcomtaskexecution/COMPLETED";
    private final static String SCHEDULED_COMTASKEXECUTION_FAILED = "com/energyict/mdc/device/data/scheduledcomtaskexecution/FAILED";
    private final static String TOU_CAMPAIGN_EDITED = "com/energyict/mdc/tou/campaign/toucampaign/EDITED";
    private TimeOfUseCampaignServiceImpl timeOfUseCampaignService;
    private Clock clock;
    private ServiceCallService serviceCallService;
    private Thesaurus thesaurus;

    @Inject
    public TimeOfUseCampaignHandler(TimeOfUseCampaignServiceImpl timeOfUseCampaignService, Clock clock, ServiceCallService serviceCallService,
                                    Thesaurus thesaurus) {
        super(LocalEvent.class);
        this.timeOfUseCampaignService = timeOfUseCampaignService;
        this.serviceCallService = serviceCallService;
        this.clock = clock;
        this.thesaurus = thesaurus;
    }

    @Override
    protected void onEvent(LocalEvent event, Object... eventDetails) {
        switch (event.getType().getTopic()) {
            case MANUAL_COMTASKEXECUTION_STARTED:
            case SCHEDULED_COMTASKEXECUTION_STARTED:
                processEvent(event, this::onComTaskStarted);
                break;
            case MANUAL_COMTASKEXECUTION_COMPLETED:
            case SCHEDULED_COMTASKEXECUTION_COMPLETED:
                processEvent(event, this::onComTaskCompleted);
                break;
            case MANUAL_COMTASKEXECUTION_FAILED:
            case SCHEDULED_COMTASKEXECUTION_FAILED:
                processEvent(event, this::onComTaskFailed);
                break;
            case TOU_CAMPAIGN_EDITED:
                CompletableFuture.runAsync(() -> timeOfUseCampaignService.editCampaignItems((TimeOfUseCampaign) event.getSource()), Executors.newSingleThreadExecutor());
                break;
            default:
                break;
        }
    }

    private void onComTaskFailed(ComTaskExecution comTaskExecution) {
        if (isForCalendar(comTaskExecution)) {
            Optional<TimeOfUseCampaign> timeOfUseCampaignOptional = timeOfUseCampaignService.getCampaignOn(comTaskExecution);
            if (timeOfUseCampaignOptional.isPresent()) {
                boolean planning = true;
                TimeOfUseCampaign timeOfUseCampaign = timeOfUseCampaignOptional.get();
                Device device = comTaskExecution.getDevice();
                if (plannedCalendarIsOnCampaign(device, timeOfUseCampaign)) {
                    if (device.calendars().getPlannedPassive()
                            .flatMap(PassiveCalendar::getDeviceMessage)
                            .map(DeviceMessage::getStatus)
                            .filter(deviceMessageStatus -> deviceMessageStatus.equals(DeviceMessageStatus.FAILED))
                            .isPresent()) {
                        ServiceCall serviceCall = timeOfUseCampaignService.findActiveTimeOfUseItemByDevice(device).get().getServiceCall();
                        serviceCallService.lockServiceCall(serviceCall.getId());
                        serviceCall.requestTransition(DefaultState.FAILED);
                        timeOfUseCampaignService.logInServiceCall(serviceCall, MessageSeeds.CALENDAR_INSTALLATION_FAILED, LogLevel.WARNING);
                        planning = false;
                    }
                }
                if (planning) {
                    if (device.getComTaskExecutions().stream()
                            .noneMatch(comTaskExecution1 -> comTaskExecution1.getNextExecutionTimestamp().equals(timeOfUseCampaign.getUploadPeriodStart()))) {
                        comTaskExecution.schedule(timeOfUseCampaign.getUploadPeriodStart());
                    }
                }
            }
        } else if (comTaskExecution.getComTask().getProtocolTasks().stream()
                .anyMatch(StatusInformationTask.class::isInstance)) {
            Optional<TimeOfUseCampaign> timeOfUseCampaignOptional = timeOfUseCampaignService.getCampaignOn(comTaskExecution);
            if (timeOfUseCampaignOptional.isPresent()) {
                TimeOfUseCampaign timeOfUseCampaign = timeOfUseCampaignOptional.get();
                if (isWithVerification(timeOfUseCampaign)) {
                    ServiceCall serviceCall = timeOfUseCampaignService.findActiveTimeOfUseItemByDevice(comTaskExecution.getDevice()).get().getServiceCall();
                    if (serviceCall.getExtension(TimeOfUseItemDomainExtension.class)
                            .flatMap(TimeOfUseItemDomainExtension::getDeviceMessage)
                            .map(DeviceMessage::getStatus)
                            .filter(deviceMessageStatus -> deviceMessageStatus.equals(DeviceMessageStatus.CONFIRMED))
                            .isPresent()) {
                        serviceCallService.lockServiceCall(serviceCall.getId());
                        serviceCall.requestTransition(DefaultState.FAILED);
                        timeOfUseCampaignService.logInServiceCall(serviceCall, MessageSeeds.VERIFICATION_FAILED, LogLevel.WARNING);
                    }
                }
            }
        }

    }

    private void onComTaskCompleted(ComTaskExecution comTaskExecution) {
        if (isForCalendar(comTaskExecution)) {
            Optional<TimeOfUseCampaign> timeOfUseCampaignOptional = timeOfUseCampaignService.getCampaignOn(comTaskExecution);
            if (timeOfUseCampaignOptional.isPresent()) {
                boolean planning = true;
                TimeOfUseCampaign timeOfUseCampaign = timeOfUseCampaignOptional.get();
                Device device = comTaskExecution.getDevice();
                if (plannedCalendarIsOnCampaign(device, timeOfUseCampaign)) {
                    if (device.calendars().getPlannedPassive()
                            .flatMap(PassiveCalendar::getDeviceMessage)
                            .map(DeviceMessage::getStatus)
                            .filter(deviceMessageStatus -> deviceMessageStatus.equals(DeviceMessageStatus.CONFIRMED))
                            .isPresent()) {
                        ServiceCall serviceCall = timeOfUseCampaignService.findActiveTimeOfUseItemByDevice(comTaskExecution.getDevice()).get().getServiceCall();
                        if (!isWithVerification(timeOfUseCampaign)) {
                            serviceCallService.lockServiceCall(serviceCall.getId());
                            serviceCall.requestTransition(DefaultState.SUCCESSFUL);
                            timeOfUseCampaignService.logInServiceCall(serviceCall, MessageSeeds.CALENDAR_INSTALLATION_COMPLETED, LogLevel.INFO);
                            timeOfUseCampaignService.revokeCalendarsCommands(device); //in case calendar has already been uploaded out of campaign scope
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
                            .noneMatch(comTaskExecution1 -> comTaskExecution1.getNextExecutionTimestamp().equals(timeOfUseCampaign.getUploadPeriodStart()))) {
                        comTaskExecution.schedule(timeOfUseCampaign.getUploadPeriodStart());
                    }
                }
            }
        } else if (comTaskExecution.getComTask().getProtocolTasks().stream()
                .anyMatch(StatusInformationTask.class::isInstance)) {
            Optional<TimeOfUseCampaign> timeOfUseCampaignOptional = timeOfUseCampaignService.getCampaignOn(comTaskExecution);
            if (timeOfUseCampaignOptional.isPresent()) {
                TimeOfUseCampaign timeOfUseCampaign = timeOfUseCampaignOptional.get();
                if (isWithVerification(timeOfUseCampaign)) {
                    if (comTaskExecution.getDevice().calendars().getActive().isPresent()) {
                        ServiceCall serviceCall = timeOfUseCampaignService.findActiveTimeOfUseItemByDevice(comTaskExecution.getDevice()).get().getServiceCall();
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
                                timeOfUseCampaignService.logInServiceCall(serviceCall, MessageSeeds.VERIFICATION_FAILED_WRONG_CALENDAR, LogLevel.WARNING);
                            }
                        }
                    }
                }
            }
        }
    }

    private void onComTaskStarted(ComTaskExecution comTaskExecution) {
        if (isForCalendar(comTaskExecution)) {
            Optional<TimeOfUseCampaign> timeOfUseCampaignOptional = timeOfUseCampaignService.getCampaignOn(comTaskExecution);
            if (timeOfUseCampaignOptional.isPresent()) {
                boolean planning = true;
                TimeOfUseCampaign timeOfUseCampaign = timeOfUseCampaignOptional.get();
                Device device = comTaskExecution.getDevice();
                TimeOfUseCampaignItem timeOfUseItem = timeOfUseCampaignService.findActiveTimeOfUseItemByDevice(device).get();
                ServiceCall serviceCall = timeOfUseItem.getServiceCall();
                if (shouldCalendarBeInstalled(device)) {
                    if (plannedCalendarIsOnCampaign(device, timeOfUseCampaign)) {
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
                        timeOfUseItem.cancel();
                        planning = false;
                    }
                } else {
                    timeOfUseItem.cancel();
                    planning = false;
                }
                if (planning) {
                    if (device.getComTaskExecutions().stream()
                            .noneMatch(comTaskExecution1 -> comTaskExecution1.getNextExecutionTimestamp().equals(timeOfUseCampaign.getUploadPeriodStart()))) {
                        comTaskExecution.schedule(timeOfUseCampaign.getUploadPeriodStart());
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

    private boolean isForCalendar(ComTaskExecution comTaskExecution) {
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
            ServiceCall serviceCall = timeOfUseCampaignService.findActiveTimeOfUseItemByDevice(device).get().getServiceCall();
            serviceCallService.lockServiceCall(serviceCall.getId());
            timeOfUseCampaignService.logInServiceCall(serviceCall, MessageSeeds.ACTIVE_VERIFICATION_TASK_ISNT_FOUND, LogLevel.WARNING);
            serviceCall.requestTransition(DefaultState.FAILED);
        }
    }

    private boolean isWithVerification(TimeOfUseCampaign timeOfUseCampaign) {
        return timeOfUseCampaign.getActivationOption().equals(TranslationKeys.IMMEDIATELY.getKey());
    }
}
