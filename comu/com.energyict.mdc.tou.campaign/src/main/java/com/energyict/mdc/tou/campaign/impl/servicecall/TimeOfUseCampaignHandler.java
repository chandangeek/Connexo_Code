/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.impl.servicecall;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.device.config.AllowedCalendar;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.ConnectionStrategy;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.PassiveCalendar;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.MessagesTask;
import com.energyict.mdc.common.tasks.StatusInformationTask;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaign;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignItem;
import com.energyict.mdc.tou.campaign.impl.MessageSeeds;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import javax.inject.Inject;
import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
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
    private ThreadPrincipalService threadPrincipalService;
    private TransactionService transactionService;

    @Inject
    public TimeOfUseCampaignHandler(TimeOfUseCampaignServiceImpl timeOfUseCampaignService, Clock clock, ServiceCallService serviceCallService,
                                    Thesaurus thesaurus, ThreadPrincipalService threadPrincipalService, TransactionService transactionService) {
        super(LocalEvent.class);
        this.timeOfUseCampaignService = timeOfUseCampaignService;
        this.serviceCallService = serviceCallService;
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.threadPrincipalService = threadPrincipalService;
        this.transactionService = transactionService;
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
                Principal principal = threadPrincipalService.getPrincipal();
                CompletableFuture.runAsync(() -> {
                    threadPrincipalService.set(principal);
                    transactionService.run(() -> timeOfUseCampaignService.editCampaignItems((TimeOfUseCampaign) event.getSource()));
                }, Executors.newSingleThreadExecutor());
                break;
            default:
                break;
        }
    }

    private void onComTaskFailed(ComTaskExecution comTaskExecution) {
        Optional<TimeOfUseCampaignItem> optionalTimeOfUseCampaignItem = timeOfUseCampaignService.findActiveTimeOfUseItemByDevice(comTaskExecution.getDevice());
        if (optionalTimeOfUseCampaignItem.isPresent()) {
            TimeOfUseItemDomainExtension timeOfUseCampaignItem = (TimeOfUseItemDomainExtension) optionalTimeOfUseCampaignItem.get();
            if (timeOfUseCampaignItem.getStepOfUpdate() == 0) {
                if (isForCalendar(comTaskExecution)) {
                    TimeOfUseCampaign timeOfUseCampaign = optionalTimeOfUseCampaignItem.get().getServiceCall().getParent().get().getExtension(TimeOfUseCampaignDomainExtension.class).get();
                    if (comTaskExecution.getComTask().getId() == timeOfUseCampaign.getCalendarUploadComTaskId()) {
                        boolean planning = true;
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
                            comTaskExecution.schedule(timeOfUseCampaign.getUploadPeriodStart());
                        }
                    }
                }
            } else if (timeOfUseCampaignItem.getStepOfUpdate() == 1) {
                if (comTaskExecution.getComTask().getProtocolTasks().stream()
                        .anyMatch(StatusInformationTask.class::isInstance)) {
                    TimeOfUseCampaign timeOfUseCampaign = optionalTimeOfUseCampaignItem.get().getServiceCall().getParent().get().getExtension(TimeOfUseCampaignDomainExtension.class).get();
                    if (comTaskExecution.getComTask().getId() == timeOfUseCampaign.getCalendarUploadComTaskId()) {
                        Instant calendarsTimeUpload = timeOfUseCampaignService.findActiveTimeOfUseItemByDevice(comTaskExecution.getDevice()).get()
                                .getServiceCall().getLastModificationTime();
                        if (calendarsTimeUpload.plusMillis(timeOfUseCampaign.getValidationTimeout()).isBefore(clock.instant())) {
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
                        } else {
                            scheduleVerification(comTaskExecution.getDevice(), calendarsTimeUpload.plusSeconds(timeOfUseCampaign.getValidationTimeout()));
                        }
                    }
                }
            }
        }
    }

    private void onComTaskCompleted(ComTaskExecution comTaskExecution) {
        Optional<TimeOfUseCampaignItem> optionalTimeOfUseCampaignItem = timeOfUseCampaignService.findActiveTimeOfUseItemByDevice(comTaskExecution.getDevice());
        if (optionalTimeOfUseCampaignItem.isPresent()) {
            TimeOfUseItemDomainExtension timeOfUseCampaignItem = (TimeOfUseItemDomainExtension) optionalTimeOfUseCampaignItem.get();
            if (timeOfUseCampaignItem.getStepOfUpdate() == 0) {
                if (isForCalendar(comTaskExecution)) {
                    boolean planning = true;
                    TimeOfUseCampaign timeOfUseCampaign = timeOfUseCampaignItem.getServiceCall().getParent().get().getExtension(TimeOfUseCampaignDomainExtension.class).get();
                    Device device = comTaskExecution.getDevice();
                    if (plannedCalendarIsOnCampaign(device, timeOfUseCampaign)) {
                        if (device.calendars().getPlannedPassive()
                                .flatMap(PassiveCalendar::getDeviceMessage)
                                .map(DeviceMessage::getStatus)
                                .filter(deviceMessageStatus -> deviceMessageStatus.equals(DeviceMessageStatus.CONFIRMED))
                                .isPresent()) {
                            ServiceCall serviceCall = timeOfUseCampaignService.findActiveTimeOfUseItemByDevice(comTaskExecution.getDevice()).get().getServiceCall();
                            if (!timeOfUseCampaignService.isWithVerification(timeOfUseCampaign)) {
                                serviceCallService.lockServiceCall(serviceCall.getId());
                                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
                                timeOfUseCampaignService.logInServiceCall(serviceCall, MessageSeeds.CALENDAR_INSTALLATION_COMPLETED, LogLevel.INFO);
                                timeOfUseCampaignService.revokeCalendarsCommands(device); //in case calendar has already been uploaded out of campaign scope
                                planning = false;
                            } else {
                                scheduleVerification(device, clock.instant().plusSeconds(timeOfUseCampaign.getValidationTimeout()));
                                timeOfUseCampaignService.logInServiceCall(serviceCall, MessageSeeds.VERIFICATION_SCHEDULED, LogLevel.INFO);
                                timeOfUseCampaignItem.setStepOfUpdate(1);
                                timeOfUseCampaignItem.update();
                                planning = false;
                            }
                        }
                    }
                    if (planning) {
                        device.getComTaskExecutions().stream()
                                .filter(comTaskExecution1 -> comTaskExecution.getComTask().getId() == timeOfUseCampaign.getCalendarUploadComTaskId())
                                .filter(comTaskExecution1 -> comTaskExecution1.getNextExecutionTimestamp() == null)
                                .findFirst().ifPresent(comTaskExecution1 -> comTaskExecution1.schedule(timeOfUseCampaign.getUploadPeriodStart()));
                    }

                }
            } else if (timeOfUseCampaignItem.getStepOfUpdate() == 1) {
                if (comTaskExecution.getComTask().getProtocolTasks().stream()
                        .anyMatch(StatusInformationTask.class::isInstance)) {
                    TimeOfUseCampaign timeOfUseCampaign = optionalTimeOfUseCampaignItem.get().getServiceCall().getParent().get().getExtension(TimeOfUseCampaignDomainExtension.class).get();
                    Instant calendarsTimeUpload = timeOfUseCampaignService.findActiveTimeOfUseItemByDevice(comTaskExecution.getDevice()).get()
                            .getServiceCall().getLastModificationTime();
                    if (calendarsTimeUpload.plusMillis(timeOfUseCampaign.getValidationTimeout()).isBefore(clock.instant())) {
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
                    } else {
                        scheduleVerification(comTaskExecution.getDevice(), calendarsTimeUpload.plusSeconds(timeOfUseCampaign.getValidationTimeout()));
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
                if (!serviceCall.getState().equals(DefaultState.ONGOING)) {
                    if (shouldCalendarBeInstalled(device)) {
                        if (plannedCalendarIsOnCampaign(device, timeOfUseCampaign)) {
                            serviceCallService.lockServiceCall(serviceCall.getId());
                            if (serviceCall.canTransitionTo(DefaultState.ONGOING)) {
                                serviceCall.requestTransition(DefaultState.ONGOING);
                                timeOfUseCampaignService.logInServiceCall(serviceCall, MessageSeeds.CALENDAR_INSTALLATION_STARTED, LogLevel.INFO);
                            }
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

    private void scheduleVerification(Device device, Instant when) {
        ServiceCall serviceCall = timeOfUseCampaignService.findActiveTimeOfUseItemByDevice(device).get().getServiceCall();
        Optional<? extends TimeOfUseCampaign> campaignOptional = serviceCall.getParent().get().getExtension(TimeOfUseCampaignDomainExtension.class);
        if (campaignOptional.isPresent()) {
            TimeOfUseCampaign campaign = campaignOptional.get();
            Optional<ComTaskEnablement> comTaskEnablementOptional = device.getDeviceConfiguration().getComTaskEnablements().stream()
                    .filter(comTaskEnablement -> comTaskEnablement.getComTask().getId() == campaign.getValidationComTaskId())
                    .filter(comTaskEnablement -> comTaskEnablement.getComTask().getProtocolTasks().stream()
                            .anyMatch(task -> task instanceof StatusInformationTask))
                    .filter(comTaskEnablement -> !comTaskEnablement.isSuspended())
                    .filter(comTaskEnablement -> (timeOfUseCampaignService.findComTaskExecution(device, comTaskEnablement) == null)
                            || (!timeOfUseCampaignService.findComTaskExecution(device, comTaskEnablement).isOnHold()))
                    .findAny();
            if (comTaskEnablementOptional.isPresent()) {
                ComTaskExecution comTaskExecution = device.getComTaskExecutions().stream()
                        .filter(cte -> cte.getComTask().equals(comTaskEnablementOptional.get().getComTask()))
                        .findAny().orElseGet(() -> device.newAdHocComTaskExecution(comTaskEnablementOptional.get()).add());
                if (comTaskExecution.getConnectionTask().isPresent()) {
                    ConnectionStrategy connectionStrategy = ((ScheduledConnectionTask) comTaskExecution.getConnectionTask().get()).getConnectionStrategy();
                    if (comTaskExecution.getConnectionTask().get().isActive() && (!campaign.getValidationConnectionStrategy()
                            .isPresent() || connectionStrategy == campaign.getValidationConnectionStrategy().get())) {
                        comTaskExecution.schedule(when);
                    } else {
                        serviceCallService.lockServiceCall(serviceCall.getId());
                        serviceCall.log(LogLevel.SEVERE, thesaurus.getSimpleFormat(MessageSeeds.CONNECTION_METHOD_DOESNT_MEET_THE_REQUIREMENT)
                                .format(campaign.getValidationConnectionStrategy().get().name(), comTaskExecution.getComTask().getName()));
                        serviceCall.requestTransition(DefaultState.FAILED);
                    }
                }
            } else {
                serviceCallService.lockServiceCall(serviceCall.getId());
                serviceCall.log(LogLevel.SEVERE, thesaurus.getSimpleFormat(MessageSeeds.TASK_FOR_VALIDATION_IS_MISSING).format());
                serviceCall.requestTransition(DefaultState.FAILED);
            }
        }

    }
}
