/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.tou.campaign.impl.servicecall;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.impl.MeteringDataModelService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.ActiveEffectiveCalendar;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.exceptions.DeviceMessageNotAllowedException;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.StatusInformationTask;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaign;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignBuilder;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignException;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;
import com.energyict.mdc.tou.campaign.TimeOfUseItem;
import com.energyict.mdc.tou.campaign.impl.EventType;
import com.energyict.mdc.tou.campaign.impl.Installer;
import com.energyict.mdc.tou.campaign.impl.MessageSeeds;
import com.energyict.mdc.tou.campaign.impl.ServiceCallTypes;
import com.energyict.mdc.tou.campaign.impl.TimeOfUseCampaignBuilderImpl;
import com.energyict.mdc.tou.campaign.impl.TranslationKeys;
import com.energyict.mdc.tou.campaign.security.Privileges;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
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
    private volatile OrmService ormService;
    private volatile EventService eventService;
    private volatile Clock clock;
    private volatile DeviceService deviceService;
    private volatile CalendarService calendarService;
    private volatile NlsService nlsService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;
    private volatile RegisteredCustomPropertySet registeredCustomPropertySet;
    private List<CustomPropertySet> customPropertySets = new ArrayList<>();
    private List<ServiceRegistration> serviceRegistrations = new ArrayList<>();


    public TimeOfUseCampaignServiceImpl() {
        //for OSGI
    }

    @Inject
    public TimeOfUseCampaignServiceImpl(ThreadPrincipalService threadPrincipalService, TransactionService transactionService,
                                        NlsService nlsService, UpgradeService upgradeService, UserService userService,
                                        BatchService batchService, PropertySpecService propertySpecService,
                                        ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService,
                                        MeteringGroupsService meteringGroupsService, OrmService ormService, Clock clock, DeviceService deviceService,
                                        CalendarService calendarService, DeviceConfigurationService deviceConfigurationService,
                                        DeviceMessageSpecificationService deviceMessageSpecificationService, EventService eventService,
                                        BundleContext bundleContext) {
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
        setOrmService(ormService);
        setEventService(eventService);
        setClock(clock);
        setDeviceService(deviceService);
        setCalendarService(calendarService);
        setDeviceConfigurationService(deviceConfigurationService);
        setDeviceMessageSpecificationService(deviceMessageSpecificationService);
        activate(bundleContext);
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
                bind(OrmService.class).toInstance(ormService);
                bind(EventService.class).toInstance(eventService);
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
    public void activate(BundleContext bundleContext) {
        dataModel = upgradeService.newNonOrmDataModel();
        dataModel.register(getModule());
        customPropertySets.add(dataModel.getInstance(TimeOfUseCampaignCustomPropertySet.class));
        customPropertySets.add(dataModel.getInstance(TimeOfUseItemPropertySet.class));
        customPropertySets.forEach(customPropertySetService::addCustomPropertySet);
        registeredCustomPropertySet = customPropertySetService.findActiveCustomPropertySet(TimeOfUseCampaignCustomPropertySet.CUSTOM_PROPERTY_SET_ID).get();
        upgradeService.register(InstallIdentifier.identifier("MultiSense", COMPONENT_NAME), dataModel, Installer.class, Collections.emptyMap());
        registerServiceCallHandler(bundleContext, dataModel.getInstance(TimeOfUseCampaignServiceCallHandler.class), TimeOfUseCampaignServiceCallHandler.NAME);
        registerServiceCallHandler(bundleContext, dataModel.getInstance(TimeOfUseItemServiceCallHandler.class), TimeOfUseItemServiceCallHandler.NAME);
        serviceRegistrations.add(bundleContext.registerService(Subscriber.class, dataModel.getInstance(TimeOfUseCampaignHandler.class), new Hashtable<>()));
    }

    private <T extends ServiceCallHandler> void registerServiceCallHandler(BundleContext bundleContext,
                                                                           T provider, String serviceName) {
        Dictionary<String, Object> properties = new Hashtable<>(ImmutableMap.of("name", serviceName));
        serviceRegistrations
                .add(bundleContext.registerService(ServiceCallHandler.class, provider, properties));
    }

    @Deactivate
    public void stop() {
        customPropertySets.forEach(customPropertySetService::removeCustomPropertySet);
        customPropertySets.clear();
        serviceRegistrations.forEach(ServiceRegistration::unregister);
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
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
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
        return Stream.of(
                Stream.of(TranslationKeys.values()),
                Stream.of(Privileges.values()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());

    }

    // for test purposes
    DataModel getDataModel() {
        return dataModel;
    }

    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    public ServiceCall createServiceCallAndTransition(TimeOfUseCampaignDomainExtension campaign) {
        return getServiceCallType(ServiceCallTypes.TIME_OF_USE_CAMPAIGN)
                .map(serviceCallType -> createServiceCall(serviceCallType, campaign)).orElseThrow(() -> new IllegalStateException("Service call type TIME_OF_USE_CAMPAIGN not found"));
    }

    private ServiceCall createServiceCall(ServiceCallType serviceCallType, TimeOfUseCampaignDomainExtension campaign) {
        ServiceCall serviceCall = serviceCallType.newServiceCall()
                .origin("MultiSense")
                .extendedWith(campaign)
                .create();
        serviceCall.requestTransition(DefaultState.ONGOING);
        return serviceCallService.getServiceCall(serviceCall.getId()).orElseThrow(() -> new IllegalStateException("Just created service call not found."));
    }

    public void createItemsOnCampaign(ServiceCall serviceCall) {
        serviceCall.getExtension(TimeOfUseCampaignDomainExtension.class).ifPresent(campaign -> {
            Map<MessageSeeds, Integer> numberOfDevices = new HashMap<>();
            List<Device> devicesByGroup = getDevicesByGroup(campaign.getDeviceGroup());
            List<Device> devicesByGroupAndType = devicesByGroup.stream()
                    .filter(device -> device.getDeviceType().getId() == campaign.getDeviceType().getId())
                    .collect(Collectors.toList());
            if (!devicesByGroupAndType.isEmpty()) {
                devicesByGroupAndType.forEach(device -> {
                    MessageSeeds messageSeeds = createChildServiceCall(serviceCall, device);
                    numberOfDevices.compute(messageSeeds, (key, value) -> value == null ? 1 : value + 1);
                });
            } else {
                serviceCallService.lockServiceCall(serviceCall.getId());
                serviceCall.requestTransition(DefaultState.CANCELLED);
                serviceCall.log(LogLevel.INFO, thesaurus.getFormat(MessageSeeds.DEVICES_WITH_GROUP_AND_TYPE_NOT_FOUND).format(campaign.getDeviceGroup(), campaign.getDeviceType().getName()));
            }
            int notAddedDevicesBecauseDifferentType = devicesByGroup.size() - devicesByGroupAndType.size();
            if (notAddedDevicesBecauseDifferentType > 0) {
                serviceCall.log(LogLevel.INFO, thesaurus.getFormat(MessageSeeds.DEVICES_WERENT_ADDED_BECAUSE_DIFFERENT_TYPE).format(notAddedDevicesBecauseDifferentType));
            }
            if (numberOfDevices.get(MessageSeeds.DEVICES_WERENT_ADDED_BECAUSE_PART_OTHER_CAMPAIGN) != null) {
                serviceCall.log(LogLevel.INFO, thesaurus.getFormat(MessageSeeds.DEVICES_WERENT_ADDED_BECAUSE_PART_OTHER_CAMPAIGN)
                        .format(numberOfDevices.get(MessageSeeds.DEVICES_WERENT_ADDED_BECAUSE_PART_OTHER_CAMPAIGN)));
            }
            if (numberOfDevices.get(MessageSeeds.DEVICES_WERENT_ADDED_BECAUSE_HAVE_THIS_CALENDAR) != null) {
                serviceCall.log(LogLevel.INFO, thesaurus.getFormat(MessageSeeds.DEVICES_WERENT_ADDED_BECAUSE_HAVE_THIS_CALENDAR)
                        .format(numberOfDevices.get(MessageSeeds.DEVICES_WERENT_ADDED_BECAUSE_HAVE_THIS_CALENDAR)));
            }
            if (numberOfDevices.get(MessageSeeds.DEVICE_WAS_ADDED) == null) {
                serviceCallService.lockServiceCall(serviceCall.getId());
                if (serviceCall.canTransitionTo(DefaultState.CANCELLED)) {
                    serviceCall.requestTransition(DefaultState.CANCELLED);
                }
                serviceCall.log(LogLevel.INFO, thesaurus.getString(MessageSeeds.CAMPAIGN_WAS_CANCELED_BECAUSE_DIDNT_RECEIVE_DEVICES.getKey(),
                        MessageSeeds.CAMPAIGN_WAS_CANCELED_BECAUSE_DIDNT_RECEIVE_DEVICES.getDefaultFormat()));
            }
        });
    }

    private MessageSeeds createChildServiceCall(ServiceCall parent, Device device) {
        Optional<ActiveEffectiveCalendar> calendar = device.calendars().getActive();
        if (calendar.map(ActiveEffectiveCalendar::getAllowedCalendar)
                .flatMap(AllowedCalendar::getCalendar)
                .filter(calendar1 -> (calendar1.getId() == parent.getExtension(TimeOfUseCampaignDomainExtension.class).get().getCalendar().getId()))
                .isPresent()) {
            return MessageSeeds.DEVICES_WERENT_ADDED_BECAUSE_HAVE_THIS_CALENDAR;
        }
        if (!findActiveServiceCallByDevice(device).isPresent()) {
            ServiceCallType serviceCallType = getServiceCallTypeOrThrowException(ServiceCallTypes.TIME_OF_USE_CAMPAIGN_ITEM);
            TimeOfUseItemDomainExtension timeOfUseItemDomainExtension = new TimeOfUseItemDomainExtension();
            timeOfUseItemDomainExtension.setDevice(device);
            timeOfUseItemDomainExtension.setParentServiceCallId(parent.getId());
            setCalendarOnDevice(parent.newChildCall(serviceCallType).extendedWith(timeOfUseItemDomainExtension).create(), device);
            return MessageSeeds.DEVICE_WAS_ADDED;
        } else {
            return MessageSeeds.DEVICES_WERENT_ADDED_BECAUSE_PART_OTHER_CAMPAIGN;
        }
    }

    private Optional<ServiceCallType> getServiceCallType(ServiceCallTypes serviceCallType) {
        return serviceCallService.findServiceCallType(serviceCallType.getTypeName(), serviceCallType.getTypeVersion());
    }

    public ServiceCallType getServiceCallTypeOrThrowException(ServiceCallTypes serviceCallType) {
        return serviceCallService.findServiceCallType(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())
                .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.COULDNT_FIND_SERVICE_CALL_TYPE)
                        .format(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())));
    }

    @Override
    public QueryStream<? extends TimeOfUseCampaign> streamAllCampaigns() {
        return ormService.getDataModel(TimeOfUseCampaignPersistenceSupport.COMPONENT_NAME).get().stream(TimeOfUseCampaignDomainExtension.class);
    }

    @Override
    public Map<DefaultState, Long> getChildrenStatusFromCampaign(long id) {
        return serviceCallService.getChildrenStatus(id);
    }

    @Override
    public TimeOfUseCampaignBuilder newTouCampaignBuilder(String name, long typeId, long calendarId) {
        return new TimeOfUseCampaignBuilderImpl(this)
                .addName(name)
                .addType(deviceConfigurationService.findDeviceType(typeId).get())
                .addCalendar(calendarService.findCalendar(calendarId).get());
    }

    @Override
    public Optional<TimeOfUseCampaign> getCampaign(long id) {
        return streamAllCampaigns().join(ServiceCall.class)
                .filter(Where.where("serviceCall.id").isEqualTo(id)).findAny().map(TimeOfUseCampaign.class::cast);
    }

    @Override
    public Optional<TimeOfUseCampaign> getCampaign(String name) {
        return streamAllCampaigns().filter(Where.where("name").isEqualTo(name)).findAny().map(TimeOfUseCampaign.class::cast);
    }

    @Override
    public Optional<TimeOfUseCampaign> getCampaignOn(ComTaskExecution comTaskExecution) {
        return findActiveServiceCallByDevice(comTaskExecution.getDevice())
                .flatMap(ServiceCall::getParent)
                .flatMap(serviceCall -> serviceCall.getExtension(TimeOfUseCampaignDomainExtension.class))
                .map(TimeOfUseCampaign.class::cast);
    }

    @Override
    public QueryStream<? extends TimeOfUseItem> streamDevicesInCampaigns() {
        return ormService.getDataModel(TimeOfUseItemPersistenceSupport.COMPONENT_NAME).get().stream(TimeOfUseItemDomainExtension.class);
    }

    @Override
    public List<DeviceType> getDeviceTypesWithCalendars() {
        return deviceConfigurationService.findAllDeviceTypes().stream()
                .filter(deviceType -> !deviceType.getAllowedCalendars().isEmpty())
                .filter(deviceType -> deviceConfigurationService.findTimeOfUseOptions(deviceType).isPresent())
                .filter(deviceType -> !deviceConfigurationService.findTimeOfUseOptions(deviceType).get().getOptions().isEmpty())
                .collect(Collectors.toList());
    }

    private List<Device> getDevicesByGroup(String deviceGroup) {
        EndDeviceGroup endDeviceGroup = meteringGroupsService.findEndDeviceGroupByName(deviceGroup)
                .orElseThrow(() -> new TimeOfUseCampaignException(thesaurus, MessageSeeds.DEVICE_GROUP_NOT_FOUND, deviceGroup));
        return deviceService.deviceQuery().select(ListOperator.IN.contains(endDeviceGroup.toSubQuery("id"), "meter"));
    }

    private void setCalendarOnDevice(ServiceCall serviceCall, Device device) {
        revokeCalendarsCommands(device);
        try {
            dataModel.getInstance(TimeOfUseSendHelper.class).setCalendarOnDevice(device, serviceCall);
        } catch (DeviceMessageNotAllowedException e) {
            serviceCallService.lockServiceCall(serviceCall.getId());
            if (serviceCall.canTransitionTo(DefaultState.REJECTED)) {
                serviceCall.requestTransition(DefaultState.REJECTED);
            }
            serviceCall.log(e.getLocalizedMessage(), e);
        }
    }

    void revokeCalendarsCommands(Device device) {
        device.getMessages().stream()
                .filter(deviceMessage -> (deviceMessage.getStatus().equals(DeviceMessageStatus.PENDING)
                        || deviceMessage.getStatus().equals(DeviceMessageStatus.WAITING)))
                .filter(deviceMessage -> deviceMessage.getSpecification().getCategory().getId() == 0)
                .forEach(DeviceMessage::revoke);
    }

    @Override
    public ServiceCall cancelDevice(Device device) {
        ServiceCall serviceCall = findActiveServiceCallByDevice(device).orElseThrow(() -> new TimeOfUseCampaignException(thesaurus, MessageSeeds.ACTIVE_SERVICE_CALL_BY_DEVICE_NOT_FOUND, device
                .getName()));
        if (serviceCall.canTransitionTo(DefaultState.CANCELLED)) {

            serviceCall.requestTransition(DefaultState.CANCELLED);
        }
        return serviceCallService.getServiceCall(serviceCall.getId()).get();
    }

    @Override
    public void cancelCampaign(long id) {
        serviceCallService.getServiceCall(id)
                .filter(serviceCall -> serviceCall.canTransitionTo(DefaultState.CANCELLED))
                .ifPresent(serviceCall -> {
                    serviceCallService.lockServiceCall(serviceCall.getId());
                    serviceCall.requestTransition(DefaultState.CANCELLED);
                    serviceCall.getExtension(TimeOfUseCampaignDomainExtension.class)
                            .ifPresent(timeOfUseCampaignDomainExtension -> serviceCall.update(timeOfUseCampaignDomainExtension));
                    serviceCall.log(LogLevel.INFO, thesaurus.getString(MessageSeeds.CANCELED_BY_USER.getKey(), MessageSeeds.CANCELED_BY_USER.getDefaultFormat()));
                });
    }

    @Override
    public void deleteCampaign(long id) {
        serviceCallService.getServiceCall(id).get().delete();
    }

    void cancelCalendarSend(ServiceCall serviceCall) {
        Device device = findDeviceByServiceCall(serviceCall).orElseThrow(() -> new TimeOfUseCampaignException(thesaurus, MessageSeeds.DEVICE_BY_SERVICE_CALL_NOT_FOUND));
        serviceCallService.lockServiceCall(serviceCall.getId());
        revokeCalendarsCommands(device);
        serviceCall.log(LogLevel.INFO, thesaurus.getString(MessageSeeds.CANCELED_BY_USER.getKey(), MessageSeeds.CANCELED_BY_USER.getDefaultFormat()));
    }

    @Override
    public ServiceCall retryDevice(Device device) {
        ServiceCall serviceCall = findActiveServiceCallByDevice(device).orElseThrow(() -> new TimeOfUseCampaignException(thesaurus, MessageSeeds.ACTIVE_SERVICE_CALL_BY_DEVICE_NOT_FOUND, device.getName()));
        if (serviceCall.canTransitionTo(DefaultState.PENDING)) {
            revokeCalendarsCommands(device);
            serviceCall.log(LogLevel.INFO, thesaurus.getString(MessageSeeds.RETRIED_BY_USER.getKey(), MessageSeeds.RETRIED_BY_USER.getDefaultFormat()));
            dataModel.getInstance(TimeOfUseSendHelper.class)
                    .setCalendarOnDevice(device, serviceCall);
            return serviceCallService.getServiceCall(serviceCall.getId()).get();
        }
        return serviceCall;
    }

    @Override
    public Optional<ServiceCall> findActiveServiceCallByDevice(Device device) {
        List<String> states = new ArrayList<>();
        states.add(DefaultState.ONGOING.getKey());
        states.add(DefaultState.PENDING.getKey());
        return streamDevicesInCampaigns().join(ServiceCall.class).join(ServiceCall.class).join(State.class)
                .filter(Where.where("device").isEqualTo(device))
                .filter(Where.where("serviceCall.parent.state.name").in(states)).findAny().map(TimeOfUseItem::getServiceCall);
    }

    private Optional<Device> findDeviceByServiceCall(ServiceCall serviceCall) {
        return serviceCall.getExtension(TimeOfUseItemDomainExtension.class).map(TimeOfUseItemDomainExtension::getDevice);
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
    public void edit(long id, String name, Instant start, Instant end) {
        Optional<ServiceCall> serviceCall = serviceCallService.getServiceCall(id);
        if (serviceCall.isPresent()) {
            TimeOfUseCampaignDomainExtension extension = serviceCall.get().getExtension(TimeOfUseCampaignDomainExtension.class).get();
            extension.setName(name);
            extension.setActivationStart(start);
            extension.setActivationEnd(end);
            serviceCall.get().update(extension);
            eventService.postEvent(EventType.TOU_CAMPAIGN_EDITED.topic(), extension);
        }
    }

    void editCampaignItems(TimeOfUseCampaign timeOfUseCampaign) {
        ormService.getDataModel(TimeOfUseItemPersistenceSupport.COMPONENT_NAME).get().stream(TimeOfUseItemDomainExtension.class).join(ServiceCall.class)
                .filter(Where.where("serviceCall.parent").isEqualTo(timeOfUseCampaign.getServiceCall()))
                .forEach(timeOfUseItemDomainExtension -> timeOfUseItemDomainExtension.getDeviceMessage().ifPresent(deviceMessage -> {
                    deviceMessage.setReleaseDate(timeOfUseCampaign.getActivationStart());
                    deviceMessage.save();
                    findCalendarsComTaskExecutions(timeOfUseItemDomainExtension.getDevice())
                            .findAny().ifPresent(comTaskExecution -> dataModel.getInstance(TimeOfUseSendHelper.class)
                            .scheduleCampaign(comTaskExecution, timeOfUseCampaign.getActivationStart(), timeOfUseCampaign.getActivationEnd()));
                }));
    }

    @Override
    public Optional<TimeOfUseCampaign> findAndLockToUCampaignByIdAndVersion(long id, long version) {
        return ormService.getDataModel(TimeOfUseCampaignPersistenceSupport.COMPONENT_NAME).get()
                .mapper(TimeOfUseCampaignDomainExtension.class).lockObjectIfVersion(version, id, registeredCustomPropertySet.getId()).map(TimeOfUseCampaign.class::cast);
    }

    @Override
    public Optional<ServiceCall> findAndLockToUItemByIdAndVersion(long id, long version) {
        return ormService.getDataModel(ServiceCallService.COMPONENT_NAME).get().mapper(ServiceCall.class)
                .lockObjectIfVersion(version, id)
                .map(ServiceCall.class::cast);
    }

    void logInServiceCall(ServiceCall serviceCall, MessageSeed message, LogLevel logLevel) {
        serviceCall.log(logLevel, thesaurus.getString(message.getKey(), message.getDefaultFormat()));
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