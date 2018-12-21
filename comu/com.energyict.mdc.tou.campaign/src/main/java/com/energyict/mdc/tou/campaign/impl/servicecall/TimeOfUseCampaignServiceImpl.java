/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.tou.campaign.impl.servicecall;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.impl.MeteringDataModelService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.app.MdcAppService;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.StatusInformationTask;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaign;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignBuilder;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;
import com.energyict.mdc.tou.campaign.TimeOfUseItem;
import com.energyict.mdc.tou.campaign.impl.Installer;
import com.energyict.mdc.tou.campaign.impl.MessageSeeds;
import com.energyict.mdc.tou.campaign.impl.ServiceCallTypes;
import com.energyict.mdc.tou.campaign.impl.TimeOfUseCampaignBuilderImpl;
import com.energyict.mdc.tou.campaign.impl.TimeOfUseCampaignException;
import com.energyict.mdc.tou.campaign.impl.TranslationKeys;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(
        name = "com.energyict.mdc.tou.campaign.impl.TimeOfUseCampaignServiceImpl",
        service = {TimeOfUseCampaignService.class, TimeOfUseCampaignServiceImpl.class, MessageSeedProvider.class, TranslationKeyProvider.class},
        property = {"name=" + TimeOfUseCampaignService.COMPONENT_NAME},
        immediate = true)
public class TimeOfUseCampaignServiceImpl implements TimeOfUseCampaignService, MessageSeedProvider, TranslationKeyProvider {

    private volatile DataModel dataModel;
    private volatile UpgradeService upgradeService;
    private volatile Thesaurus thesaurus;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile UserService userService;
    private volatile PropertySpecService propertySpecService;
    private volatile BatchService batchService;
    private volatile ServiceCallService serviceCallService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile Clock clock;
    private volatile DeviceService deviceService;
    private volatile CalendarService calendarService;
    private volatile NlsService nlsService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;
    private List<CustomPropertySet> customPropertySets = new ArrayList<>();


    public TimeOfUseCampaignServiceImpl() {
        //for OSGI
    }

    @Inject
    public TimeOfUseCampaignServiceImpl(ThreadPrincipalService threadPrincipalService, TransactionService transactionService,
                                        NlsService nlsService, UpgradeService upgradeService, UserService userService,
                                        BatchService batchService, PropertySpecService propertySpecService,
                                        ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService,
                                        MeteringGroupsService meteringGroupsService, Clock clock, DeviceService deviceService,
                                        CalendarService calendarService, DeviceConfigurationService deviceConfigurationService,
                                        DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this();
        setThreadPrincipalService(threadPrincipalService);
        setTransactionService(transactionService);
        setNlsService(nlsService);
        setUpgradeService(upgradeService);
        setUserService(userService);
        setBatchService(batchService);
        setPropertySpecService(propertySpecService);
        setServiceCallService(serviceCallService);
        setCustomPropertySetService(customPropertySetService);
        setMeteringGroupsService(meteringGroupsService);
        setClock(clock);
        setDeviceService(deviceService);
        setCalendarService(calendarService);
        setDeviceConfigurationService(deviceConfigurationService);
        setDeviceMessageSpecificationService(deviceMessageSpecificationService);
        activate();
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(TransactionService.class).toInstance(transactionService);
                bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
                bind(UserService.class).toInstance(userService);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(BatchService.class).toInstance(batchService);
                bind(ServiceCallService.class).toInstance(serviceCallService);
                bind(CustomPropertySetService.class).toInstance(customPropertySetService);
                bind(MeteringGroupsService.class).toInstance(meteringGroupsService);
                bind(Clock.class).toInstance(clock);
                bind(DeviceService.class).toInstance(deviceService);
                bind(CalendarService.class).toInstance(calendarService);
                bind(NlsService.class).toInstance(nlsService);
                bind(DeviceConfigurationService.class).toInstance(deviceConfigurationService);
                bind(DeviceMessageSpecificationService.class).toInstance(deviceMessageSpecificationService);
                bind(TimeOfUseCampaignService.class).toInstance(TimeOfUseCampaignServiceImpl.this);
                bind(TimeOfUseCampaignServiceImpl.class).toInstance(TimeOfUseCampaignServiceImpl.this);
            }
        };
    }

    @Activate
    public void activate() {
        dataModel = upgradeService.newNonOrmDataModel();
        dataModel.register(getModule());
        customPropertySets.add(dataModel.getInstance(TimeOfUseCampaignCustomPropertySet.class));
        customPropertySets.add(dataModel.getInstance(TimeOfUseItemPropertySet.class));
        customPropertySets.forEach(customPropertySetService::addCustomPropertySet);
        upgradeService.register(InstallIdentifier.identifier(MdcAppService.APPLICATION_NAME, COMPONENT_NAME), dataModel, Installer.class, Collections.emptyMap());
    }

    @Deactivate
    public void stop() {
        customPropertySets.forEach(customPropertySetService::removeCustomPropertySet);
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, getLayer())
                .join(nlsService.getThesaurus(MeteringDataModelService.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setBatchService(BatchService batchService) {
        this.batchService = batchService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setCalendarService(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setDeviceMessageSpecificationService(DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(TranslationKeys.values());
    }

    // for test purposes
    DataModel getDataModel() {
        return dataModel;
    }

    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    public void createServiceCallAndTransition(TimeOfUseCampaign campaign) {
        getServiceCallType(ServiceCallTypes.TIME_OF_USE_CAMPAIGN)
                .ifPresent(serviceCallType -> createServiceCall(serviceCallType, campaign));
    }

    private void createServiceCall(ServiceCallType serviceCallType, TimeOfUseCampaign campaign) {
        TimeOfUseCampaignDomainExtension timeOfUseCampaignDomainExtension =
                new TimeOfUseCampaignDomainExtension(thesaurus);
        timeOfUseCampaignDomainExtension.setName(campaign.getName());
        timeOfUseCampaignDomainExtension.setDeviceType(campaign.getDeviceType());
        timeOfUseCampaignDomainExtension.setDeviceGroup(campaign.getDeviceGroup());
        timeOfUseCampaignDomainExtension.setActivationStart(campaign.getActivationStart());
        timeOfUseCampaignDomainExtension.setActivationEnd(campaign.getActivationEnd());
        timeOfUseCampaignDomainExtension.setCalendar(campaign.getCalendar());
        timeOfUseCampaignDomainExtension.setActivationDate(campaign.getActivationDate());
        timeOfUseCampaignDomainExtension.setUpdateType(campaign.getUpdateType());
        timeOfUseCampaignDomainExtension.setTimeValidation(campaign.getTimeValidation());
        ServiceCall serviceCall = serviceCallType.newServiceCall()
                .origin(MdcAppService.APPLICATION_NAME)
                .extendedWith(timeOfUseCampaignDomainExtension)
                .create();
        serviceCall.requestTransition(DefaultState.ONGOING);
    }

    void createItemsOnCampaign(ServiceCall serviceCall) {
        if (serviceCall.getExtension(TimeOfUseCampaignDomainExtension.class).isPresent()) {
            TimeOfUseCampaign campaign = serviceCall.getExtension(TimeOfUseCampaignDomainExtension.class).get();
            final int[] numberOfDevices = {0, 0, 0, 0};
            if (!getDevices(campaign.getDeviceGroup(), campaign.getDeviceType()).isEmpty()) {
                getDevices(campaign.getDeviceGroup(), campaign.getDeviceType())
                        .forEach(device1 -> {
                            switch (createChildServiceCall(serviceCall, buildToUItem(device1))) {
                                case 2:
                                    numberOfDevices[0]++;
                                    break;
                                case 0:
                                    numberOfDevices[1]++;
                                    break;
                                case 1:
                                    numberOfDevices[2]++;
                                    break;
                            }
                        });
            } else {
                serviceCall.requestTransition(DefaultState.CANCELLED);
                serviceCall.log(LogLevel.INFO, "devices with group " + campaign.getDeviceGroup() + " and type " + campaign.getDeviceType() + " not found");
            }
            numberOfDevices[3] = meteringGroupsService.findEndDeviceGroupByName(campaign.getDeviceGroup())
                    .get()
                    .getMembers(clock.instant())
                    .size() - getDevices(campaign.getDeviceGroup(), campaign.getDeviceType()).size();
            if (numberOfDevices[3] > 0) {
                serviceCall.log(LogLevel.INFO, numberOfDevices[3] + " devices were not added to the campaign because they are of a different type");
            }
            if (numberOfDevices[0] > 0) {
                serviceCall.log(LogLevel.INFO, numberOfDevices[0] + " devices were not added to the campaign because they are part of other ongoing campaigns");
            }
            if (numberOfDevices[1] > 0) {
                serviceCall.log(LogLevel.INFO, numberOfDevices[1] + " devices were not added to the campaign because they already have this calendar");
            }
            if (numberOfDevices[2] == 0) {
                if (serviceCall.canTransitionTo(DefaultState.CANCELLED)) {
                    serviceCall.requestTransition(DefaultState.CANCELLED);
                }
                serviceCall.log(LogLevel.INFO, "campaign was cancelled because did not receive devices");
            }
        }
    }

    private byte createChildServiceCall(ServiceCall parent, TimeOfUseItem timeOfUseItem) {
        if (deviceService.findDeviceByName(timeOfUseItem.getDevice().getName()).get().calendars().getActive().isPresent()) {
            if (deviceService.findDeviceByName(timeOfUseItem.getDevice().getName()).get().calendars().getActive().get().getAllowedCalendar().getId()
                    == parent.getExtension(TimeOfUseCampaignDomainExtension.class).get().getCalendar().getId()) {
                return 0;
            }
        }
        if (findServiceCallsByDevice(timeOfUseItem.getDevice())
                .noneMatch(serviceCall -> (serviceCall.getParent().get().getState().equals(DefaultState.ONGOING)
                        || serviceCall.getParent().get().getState().equals(DefaultState.PENDING)))) {
            ServiceCallType serviceCallType = getServiceCallTypeOrThrowException(ServiceCallTypes.TIME_OF_USE_CAMPAIGN_ITEM);
            TimeOfUseItemDomainExtension timeOfUseItemDomainExtension = new TimeOfUseItemDomainExtension();
            timeOfUseItemDomainExtension.setParentServiceCallId(BigDecimal.valueOf(parent.getId()));
            timeOfUseItemDomainExtension.setDevice(timeOfUseItem.getDevice());
            setCalendarOnDevice(parent.newChildCall(serviceCallType)
                    .extendedWith(timeOfUseItemDomainExtension)
                    .create());
            return 1;
        } else {
            return 2;
        }
    }

    private Optional<ServiceCallType> getServiceCallType(ServiceCallTypes serviceCallType) {
        return serviceCallService.findServiceCallType(serviceCallType.getTypeName(), serviceCallType.getTypeVersion());
    }

    public ServiceCallType getServiceCallTypeOrThrowException(ServiceCallTypes serviceCallType) {
        return serviceCallService.findServiceCallType(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())
                .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.COULD_NOT_FIND_SERVICE_CALL_TYPE)
                        .format(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())));
    }

    @Override
    public List<TimeOfUseCampaign> getAllCampaigns() {
        return serviceCallService.getServiceCallFinder().find().stream()
                .filter(serviceCall -> serviceCall.getExtension(TimeOfUseCampaignDomainExtension.class).isPresent())
                .map(serviceCall -> serviceCall.getExtension(TimeOfUseCampaignDomainExtension.class).get())
                .collect(Collectors.toList());
    }

    @Override
    public TimeOfUseCampaignBuilder newToUbuilder(String name, long deviceType, String deviceGroup, Instant activationStart,
                                                  Instant activationEnd, long calendar, String activationDate,
                                                  String updateType, long timeValidation) {
        return new TimeOfUseCampaignBuilderImpl(name, deviceConfigurationService.findDeviceType(deviceType).get(),
                deviceGroup, activationStart, activationEnd, calendarService.findCalendar(calendar).get(),
                activationDate, updateType, timeValidation);
    }

    @Override
    public Optional<TimeOfUseCampaign> getCampaign(long id) {
        return serviceCallService.getServiceCall(id)
                .flatMap(serviceCall -> serviceCall.getExtension(TimeOfUseCampaignDomainExtension.class))
                .map(TimeOfUseCampaign.class::cast);
    }

    @Override
    public Optional<TimeOfUseCampaign> getCampaign(String name) {
        return serviceCallService.getServiceCallFinder().find().stream()
                .filter(serviceCall -> serviceCall.getExtension(TimeOfUseCampaignDomainExtension.class).isPresent())
                .filter(serviceCall -> serviceCall.getExtension(TimeOfUseCampaignDomainExtension.class).get().getName().equals(name))
                .findAny().map(serviceCall -> serviceCall.getExtension(TimeOfUseCampaignDomainExtension.class).get());
    }

    @Override
    public Optional<TimeOfUseCampaign> getCampaignOn(ComTaskExecution comTaskExecution) {
        return findServiceCallsByDevice(comTaskExecution.getDevice())
                .filter(serviceCall -> serviceCall.getParent().isPresent())
                .filter(serviceCall -> (serviceCall.getParent().get().getState().equals(DefaultState.ONGOING)
                        || serviceCall.getParent().get().getState().equals(DefaultState.PENDING)))
                .findAny().map(serviceCall -> serviceCall.getParent().get().getExtension(TimeOfUseCampaignDomainExtension.class).get());
    }

    @Override
    public List<DeviceType> getDeviceTypesWithCalendars() {
        return deviceConfigurationService.findAllDeviceTypes().stream()
                .filter(deviceType -> !deviceType.getAllowedCalendars().isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public void createToUCampaign(TimeOfUseCampaign timeOfUseCampaign) {
        createServiceCallAndTransition(timeOfUseCampaign);
    }

    private List<Device> getDevices(String deviceGroup, DeviceType deviceType) {
        List<Device> devices = new ArrayList<>();
        meteringGroupsService.findEndDeviceGroupByName(deviceGroup)
                .orElseThrow(() -> new TimeOfUseCampaignException(thesaurus, MessageSeeds.DEVICE_GROUP_NOT_FOUND, deviceGroup))
                .getMembers(clock.instant())
                .forEach(endDevice -> devices.add(deviceService.findDeviceByMeterId(endDevice.getId())
                        .orElseThrow(() -> new TimeOfUseCampaignException(thesaurus, MessageSeeds.DEVICE_BY_METER_ID_NOT_FOUND, endDevice.getId()))));
        return devices.stream()
                .filter(device -> device.getDeviceType().getId() == deviceType.getId())
                .collect(Collectors.toList());
    }

    private TimeOfUseItem buildToUItem(Device device) {
        TimeOfUseItemDomainExtension timeOfUseItemDomainExtension = new TimeOfUseItemDomainExtension();
        timeOfUseItemDomainExtension.setDevice(device);
        return timeOfUseItemDomainExtension;
    }

    private void setCalendarOnDevice(ServiceCall serviceCall) {
        Device device = deviceService.findDeviceByName(serviceCall.getExtension(TimeOfUseItemDomainExtension.class).get().getDevice().getName()).get();
        revokeCalendarsCommands(device);
        dataModel.getInstance(TimeOfUseSendHelper.class).setCalendarOnDevice(device, serviceCall);
    }

    void revokeCalendarsCommands(Device device) {
        device.getMessages().stream()
                .filter(deviceMessage -> (deviceMessage.getStatus().equals(DeviceMessageStatus.PENDING)
                        || deviceMessage.getStatus().equals(DeviceMessageStatus.WAITING)))
                .filter(deviceMessage -> deviceMessage.getSpecification().getCategory().getId() == 0)
                .forEach(DeviceMessage::revoke);
    }

    void changeServiceCallStatus(Device device, DefaultState defaultState) {
        findServiceCallsByDevice(device)
                .filter(serviceCall1 -> (serviceCall1.getParent().get().getState().equals(DefaultState.ONGOING)
                        || serviceCall1.getParent().get().getState().equals(DefaultState.PENDING)))
                .filter(serviceCall1 -> serviceCall1.canTransitionTo(defaultState))
                .findAny().ifPresent(serviceCall1 -> serviceCall1.requestTransition(defaultState));
    }

    @Override
    public void cancelDevice(Device device) {
        cancelCalendarSend(findActiveServiceCallByDevice(device).get());
    }

    @Override
    public void cancelCampaign(String campaign) {
        findCampaignServiceCall(campaign)
                .ifPresent(serviceCall -> {
                    serviceCall.findChildren().stream().forEach(this::cancelCalendarSend);
                    serviceCall.log(LogLevel.INFO, "campaign cancelled by user");
                });
    }

    void cancelCalendarSend(ServiceCall serviceCall) {
        revokeCalendarsCommands(findDeviceByServiceCall(serviceCall));
        findCalendarsComTaskExecutions(findDeviceByServiceCall(serviceCall)).findAny().ifPresent(comTaskExecution -> comTaskExecution.schedule(null));
        changeServiceCallStatus(findDeviceByServiceCall(serviceCall), DefaultState.CANCELLED);
        serviceCall.log(LogLevel.INFO, "cancelled by user");
    }

    @Override
    public void retry(Device device) {
        findServiceCallsByDevice(device)
                .filter(serviceCall1 -> serviceCall1.getParent().isPresent())
                .filter(serviceCall1 -> (serviceCall1.getParent().get().getState().equals(DefaultState.ONGOING)
                        || serviceCall1.getParent().get().getState().equals(DefaultState.PENDING)))
                .filter(serviceCall1 -> serviceCall1.canTransitionTo(DefaultState.PENDING)).findAny()
                .ifPresent(serviceCall1 -> {
                    revokeCalendarsCommands(findDeviceByServiceCall(serviceCall1));
                    serviceCall1.log(LogLevel.INFO, "retrying by user");
                    dataModel.getInstance(TimeOfUseSendHelper.class)
                            .setCalendarOnDevice(deviceService.findDeviceById(device.getId()).get(), serviceCall1);
                });
    }

    private Optional<ServiceCall> findActiveServiceCallByDevice(Device device) {
        return findServiceCallsByDevice(device)
                .filter(serviceCall1 -> serviceCall1.getParent().isPresent())
                .filter(serviceCall1 -> (serviceCall1.getState().equals(DefaultState.ONGOING)
                        || serviceCall1.getState().equals(DefaultState.PENDING))).findAny();
    }

    private Stream<ServiceCall> findServiceCallsByDevice(Device device) {
        return serviceCallService.getServiceCallFinder().find().stream()
                .filter(serviceCall1 -> serviceCall1.getExtension(TimeOfUseItemDomainExtension.class).isPresent())
                .filter(serviceCall1 -> serviceCall1.getExtension(TimeOfUseItemDomainExtension.class).get().getDevice().equals(device));
    }

    private Device findDeviceByServiceCall(ServiceCall serviceCall) {
        if (serviceCall.getExtension(TimeOfUseItemDomainExtension.class).isPresent()) {
            return deviceService.findDeviceByName(serviceCall.getExtension(TimeOfUseItemDomainExtension.class).get().getDevice().getName()).orElse(null);
        } else {
            return null;
        }
    }

    private Stream<ComTaskExecution> findCalendarsComTaskExecutions(Device device) {
        return device.getComTaskExecutions().stream()
                .filter(comTaskExecution -> comTaskExecution.getComTask().getProtocolTasks().stream()
                        .filter(task -> task instanceof MessagesTask)
                        .map(task -> ((MessagesTask) task))
                        .map(MessagesTask::getDeviceMessageCategories)
                        .flatMap(List::stream)
                        .anyMatch(deviceMessageCategory -> deviceMessageCategory.getId() == 0));
    }

    @Override
    public void edit(String name, TimeOfUseCampaign timeOfUseCampaign) {
        Optional<ServiceCall> serviceCall = findCampaignServiceCall(name);
        if (serviceCall.isPresent()) {
            TimeOfUseCampaignDomainExtension extension = serviceCall.get().getExtension(TimeOfUseCampaignDomainExtension.class).get();
            extension.setName(timeOfUseCampaign.getName());
            extension.setActivationStart(timeOfUseCampaign.getActivationStart());
            extension.setActivationEnd(timeOfUseCampaign.getActivationEnd());
            serviceCall.get().update(extension);
            serviceCall.get().findChildren().stream()
                    .map(this::findDeviceByServiceCall)
                    .forEach(device -> {
                        device.getMessages().stream()
                                .filter(deviceMessage -> deviceMessage.getReleaseDate().equals(timeOfUseCampaign.getActivationStart()))
                                .filter(deviceMessage -> deviceMessage.getSpecification().getCategory().getId() == 0)
                                .filter(deviceMessage -> (deviceMessage.getStatus().equals(DeviceMessageStatus.PENDING)
                                        || (deviceMessage.getStatus().equals(DeviceMessageStatus.WAITING))))
                                .findAny().ifPresent(deviceMessage -> deviceMessage.setReleaseDate(timeOfUseCampaign.getActivationStart()));
                        findCalendarsComTaskExecutions(device)
                                .findAny().ifPresent(comTaskExecution -> dataModel.getInstance(TimeOfUseSendHelper.class)
                                .scheduleCampaign(comTaskExecution, timeOfUseCampaign.getActivationStart(), timeOfUseCampaign.getActivationEnd()));
                    });
        }

    }

    private Optional<ServiceCall> findCampaignServiceCall(String campaignName) {
        return serviceCallService.getServiceCallFinder().find().stream()
                .filter(serviceCall1 -> serviceCall1.getExtension(TimeOfUseCampaignDomainExtension.class).isPresent())
                .filter(serviceCall1 -> serviceCall1.getExtension(TimeOfUseCampaignDomainExtension.class).get().getName().equals(campaignName))
                .findAny();
    }

    void logInServiceCallByDevice(Device device, String message, LogLevel logLevel) {
        findActiveServiceCallByDevice(device).ifPresent(serviceCall -> serviceCall.log(logLevel, message));
    }

    ComTaskExecution findComTaskExecution(Device device, ComTaskEnablement comTaskEnablement) {
        return device.getComTaskExecutions().stream()
                .filter(comTaskExecution1 -> comTaskExecution1.getComTask().equals(comTaskEnablement.getComTask()))
                .findAny().orElse(null);
    }

    Optional<ComTaskEnablement> getActiveVerificationTask(Device device) {
        return device.getDeviceConfiguration().getComTaskEnablements().stream()
                .filter(comTaskEnablement -> comTaskEnablement.getComTask().getProtocolTasks().stream()
                        .anyMatch(task -> task instanceof StatusInformationTask))
                .filter(comTaskEnablement -> !comTaskEnablement.isSuspended())
                .filter(comTaskEnablement -> comTaskEnablement.getComTask().getProtocolTasks().stream()
                        .noneMatch(protocolTask -> protocolTask instanceof MessagesTask))
                .filter(comTaskEnablement -> (findComTaskExecution(device, comTaskEnablement) == null)
                        || (!findComTaskExecution(device, comTaskEnablement).isOnHold()))
                .findAny();
    }

    Optional<ComTaskEnablement> getActiveTaskForCalendars(Device device) {
        return device.getDeviceConfiguration().getComTaskEnablements().stream()
                .filter(comTaskEnablement -> comTaskEnablement.getComTask().getProtocolTasks().stream()
                        .filter(task -> task instanceof MessagesTask)
                        .map(task -> ((MessagesTask) task))
                        .map(MessagesTask::getDeviceMessageCategories)
                        .flatMap(List::stream)
                        .anyMatch(deviceMessageCategory -> deviceMessageCategory.getId() == 0))
                .filter(comTaskEnablement -> !comTaskEnablement.isSuspended())
                .filter(comTaskEnablement -> comTaskEnablement.getComTask().getProtocolTasks().stream()
                        .noneMatch(protocolTask -> protocolTask instanceof StatusInformationTask))
                .filter(comTaskEnablement -> (findComTaskExecution(device, comTaskEnablement) == null)
                        || (!findComTaskExecution(device, comTaskEnablement).isOnHold()))
                .findAny();
    }
}