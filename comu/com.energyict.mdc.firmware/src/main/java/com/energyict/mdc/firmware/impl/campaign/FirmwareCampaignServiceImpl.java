/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl.campaign;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;
import com.energyict.mdc.firmware.DevicesInFirmwareCampaignFilter;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaignBuilder;
import com.energyict.mdc.firmware.FirmwareCampaignService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.impl.EventType;
import com.energyict.mdc.firmware.impl.FirmwareServiceImpl;
import com.energyict.mdc.firmware.impl.MessageSeeds;
import com.energyict.mdc.firmware.impl.TranslationKeys;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.firmware.BaseFirmwareVersion;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;

import com.google.common.collect.ImmutableMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class FirmwareCampaignServiceImpl implements FirmwareCampaignService {
    private final FirmwareServiceImpl firmwareService;
    private final DataModel dataModel;
    private final DeviceService deviceService;
    private final ServiceCallService serviceCallService;
    private final OrmService ormService;
    private final Thesaurus thesaurus;
    private final MeteringGroupsService meteringGroupsService;
    private List<ServiceRegistration> serviceRegistrations = new ArrayList<>();
    private final RegisteredCustomPropertySet registeredCustomPropertySet;
    private final EventService eventService;
    private final TaskService taskService;

    @Inject
    public FirmwareCampaignServiceImpl(FirmwareServiceImpl firmwareService, DeviceService deviceService,
                                       ServiceCallService serviceCallService, EventService eventService,
                                       Thesaurus thesaurus, MeteringGroupsService meteringGroupsService,
                                       TaskService taskService) {
        this.firmwareService = firmwareService;
        this.dataModel = firmwareService.getDataModel();
        this.ormService = firmwareService.getOrmService();
        this.registeredCustomPropertySet = firmwareService.getRegisteredCustomPropertySet();
        this.deviceService = deviceService;
        this.serviceCallService = serviceCallService;
        this.thesaurus = thesaurus;
        this.meteringGroupsService = meteringGroupsService;
        this.eventService = eventService;
        this.taskService = taskService;
    }

    public ServiceCall createServiceCallAndTransition(FirmwareCampaignDomainExtension campaign) {
        return getServiceCallType(ServiceCallTypes.FIRMWARE_CAMPAIGN)
                .map(serviceCallType -> createServiceCall(serviceCallType, campaign)).orElseThrow(() -> new IllegalStateException("Service call type TIME_OF_USE_CAMPAIGN not found"));
    }

    private ServiceCall createServiceCall(ServiceCallType serviceCallType, FirmwareCampaignDomainExtension campaign) {
        ServiceCall serviceCall = serviceCallType.newServiceCall()
                .origin("MultiSense")
                .extendedWith(campaign)
                .create();
        postEvent(EventType.FIRMWARE_CAMPAIGN_CREATED, campaign);
        serviceCall.requestTransition(DefaultState.ONGOING);
        return serviceCallService.getServiceCall(serviceCall.getId()).orElseThrow(() -> new IllegalStateException("Just created service call not found."));
    }

    private Optional<ServiceCallType> getServiceCallType(ServiceCallTypes serviceCallType) {
        return serviceCallService.findServiceCallType(serviceCallType.getTypeName(), serviceCallType.getTypeVersion());
    }

    @Override
    public ComTask getComTaskById(long id){
        return taskService.findComTask(id).get();
    }

    @Override
    public Optional<FirmwareCampaign> getFirmwareCampaignById(long id) {
        return streamAllCampaigns().join(ServiceCall.class)
                .filter(Where.where("serviceCall.id").isEqualTo(id)).findAny().map(FirmwareCampaign.class::cast);
    }

    @Override
    public Optional<FirmwareCampaign> getCampaignByName(String name) {
        return streamAllCampaigns().filter(Where.where("name").isEqualTo(name)).findAny().map(FirmwareCampaign.class::cast);
    }

    @Override
    public Optional<FirmwareCampaign> findAndLockFirmwareCampaignByIdAndVersion(long id, long version) {
        return ormService.getDataModel(FirmwareCampaignPersistenceSupport.COMPONENT_NAME).get()
                .mapper(FirmwareCampaignDomainExtension.class).lockObjectIfVersion(version, id, registeredCustomPropertySet.getId()).map(FirmwareCampaign.class::cast);
    }

    @Override
    public DevicesInFirmwareCampaignFilter filterForDevicesInFirmwareCampaign() {
        return new DevicesInFirmwareCampaignFilterImpl(this, deviceService);
    }

    @Override
    public Finder<? extends DeviceInFirmwareCampaign> getDevicesForFirmwareCampaign(DevicesInFirmwareCampaignFilter filter) {
        return DefaultFinder.of(FirmwareCampaignItemDomainExtension.class, filter.getCondition(), dataModel, ServiceCall.class, State.class);
    }

    public void createItemsOnCampaign(ServiceCall serviceCall) {
        serviceCall.getExtension(FirmwareCampaignDomainExtension.class).ifPresent(campaign -> {
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
            if (numberOfDevices.get(MessageSeeds.DEVICE_WAS_ADDED) == null) {
                serviceCallService.lockServiceCall(serviceCall.getId());
                if (serviceCall.canTransitionTo(DefaultState.CANCELLED)) {
                    serviceCall.requestTransition(DefaultState.CANCELLED);
                }
                serviceCall.log(LogLevel.INFO, thesaurus.getSimpleFormat(MessageSeeds.CAMPAIGN_WAS_CANCELED_BECAUSE_DIDNT_RECEIVE_DEVICES).format());
            }
        });
    }

    private List<Device> getDevicesByGroup(String deviceGroup) {
        EndDeviceGroup endDeviceGroup = meteringGroupsService.findEndDeviceGroupByName(deviceGroup)
                .orElseThrow(() -> new FirmwareCampaignException(thesaurus, MessageSeeds.DEVICE_GROUP_ISNT_FOUND, deviceGroup));
        return deviceService.deviceQuery().select(ListOperator.IN.contains(endDeviceGroup.toSubQuery("id"), "meter"));
    }

    private MessageSeeds createChildServiceCall(ServiceCall parent, Device device) {
        if (!findActiveFirmwareItemByDevice(device).isPresent()) {
            ServiceCallType serviceCallType = getServiceCallTypeOrThrowException(ServiceCallTypes.FIRMWARE_CAMPAIGN_ITEM);
            FirmwareCampaignItemDomainExtension firmwareCampaignItemDomainExtension = dataModel.getInstance(FirmwareCampaignItemDomainExtension.class);
            firmwareCampaignItemDomainExtension.setDevice(device);
            firmwareCampaignItemDomainExtension.setParent(parent);
            ServiceCall serviceCall = parent.newChildCall(serviceCallType).extendedWith(firmwareCampaignItemDomainExtension).targetObject(device).create();
            postEvent(EventType.DEVICE_IN_FIRMWARE_CAMPAIGN_CREATED, firmwareCampaignItemDomainExtension);
            serviceCall.requestTransition(DefaultState.PENDING);
            return MessageSeeds.DEVICE_WAS_ADDED;
        } else {
            return MessageSeeds.DEVICES_WERENT_ADDED_BECAUSE_PART_OTHER_CAMPAIGN;
        }
    }

    public ServiceCallType getServiceCallTypeOrThrowException(ServiceCallTypes serviceCallType) {
        return serviceCallService.findServiceCallType(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())
                .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.COULDNT_FIND_SERVICE_CALL_TYPE)
                        .format(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())));
    }

    public void handleFirmwareUploadCancellation(ServiceCall serviceCall) {
        FirmwareCampaignItemDomainExtension firmwareCampaignItemDomainExtension = serviceCall.getExtension(FirmwareCampaignItemDomainExtension.class).get();
        Device device = firmwareCampaignItemDomainExtension.getDevice();
        firmwareService.cancelFirmwareUploadForDevice(device);
        postEvent(EventType.DEVICE_IN_FIRMWARE_CAMPAIGN_CANCEL, firmwareCampaignItemDomainExtension);
    }

    @Override
    public FirmwareCampaignBuilder newFirmwareCampaign(String name) {
        return new FirmwareCampaignBuilderImpl(this, dataModel)
                .withName(name);
    }

    public void handleCampaignUpdate(FirmwareCampaign firmwareCampaign) {
        ormService.getDataModel(FirmwareCampaignItemPersistenceSupport.COMPONENT_NAME).get()
                .stream(FirmwareCampaignItemDomainExtension.class).join(ServiceCall.class)
                .filter(Where.where("serviceCall.parent").isEqualTo(firmwareCampaign.getServiceCall()))
                .forEach(firmwareCampaignItemDomainExtension -> firmwareCampaignItemDomainExtension.getDeviceMessage().ifPresent(deviceMessage -> {
                    deviceMessage.setReleaseDate(firmwareCampaign.getUploadPeriodStart());
                    deviceMessage.save();
                    findFirmwareComTaskExecution(firmwareCampaignItemDomainExtension.getDevice()).get().schedule(firmwareCampaign.getUploadPeriodStart());
                }));
    }

    private Optional<ComTaskExecution> findFirmwareComTaskExecution(Device device) {
        return device.getComTaskExecutions().stream().filter(ComTaskExecution::isFirmware).findAny();
    }

    @Override
    public Optional<FirmwareCampaign> getCampaignOn(ComTaskExecution comTaskExecution) {
        Optional<DeviceInFirmwareCampaign> deviceInFirmwareCampaign = findActiveFirmwareItemByDevice(comTaskExecution.getDevice());
        return deviceInFirmwareCampaign.map(deviceInFirmwareCampaign1 -> deviceInFirmwareCampaign1.getParent().getExtension(FirmwareCampaignDomainExtension.class).get());
    }

    @Override
    public Optional<DeviceInFirmwareCampaign> findActiveFirmwareItemByDevice(Device device) {
        List<String> states = new ArrayList<>();
        states.add(DefaultState.ONGOING.getKey());
        states.add(DefaultState.PENDING.getKey());
        return streamDevicesInCampaigns().join(ServiceCall.class).join(ServiceCall.class).join(State.class)
                .filter(Where.where("device").isEqualTo(device))
                .filter(Where.where("serviceCall.parent.state.name").in(states)).findAny().map(DeviceInFirmwareCampaign.class::cast);
    }

    @Override
    public QueryStream<? extends DeviceInFirmwareCampaign> streamDevicesInCampaigns() {
        return ormService.getDataModel(FirmwareCampaignItemPersistenceSupport.COMPONENT_NAME).get().stream(FirmwareCampaignItemDomainExtension.class);
    }

    @Override
    public QueryStream<? extends FirmwareCampaign> streamAllCampaigns() {
        return ormService.getDataModel(FirmwareCampaignPersistenceSupport.COMPONENT_NAME).get().stream(FirmwareCampaignDomainExtension.class);
    }

    @Override
    public List<FirmwareCampaign> findFirmwareCampaigns(DeviceType deviceType) {
        return streamAllCampaigns().filter(firmwareCampaign -> firmwareCampaign.getDeviceType().equals(deviceType)).collect(Collectors.toList());
    }

    public DataModel getDataModel() {
        return dataModel;
    }

    ComTaskExecution findComTaskExecution(Device device, ComTaskEnablement comTaskEnablement) {
        return device.getComTaskExecutions().stream()
                .filter(comTaskExecution1 -> comTaskExecution1.getComTask().equals(comTaskEnablement.getComTask()))
                .findAny().orElse(null);
    }

    private <T extends ServiceCallHandler> void registerServiceCallHandler(BundleContext bundleContext,
                                                                           T provider, String serviceName) {
        Dictionary<String, Object> properties = new Hashtable<>(ImmutableMap.of("name", serviceName));
        serviceRegistrations
                .add(bundleContext.registerService(ServiceCallHandler.class, provider, properties));
    }

    public FirmwareService getFirmwareService() {
        return firmwareService;
    }

    public void activate(BundleContext bundleContext) {
        registerServiceCallHandler(bundleContext, dataModel.getInstance(FirmwareCampaignServiceCallHandler.class), FirmwareCampaignServiceCallHandler.NAME);
        registerServiceCallHandler(bundleContext, dataModel.getInstance(FirmwareCampaignItemServiceCallHandler.class), FirmwareCampaignItemServiceCallHandler.NAME);
        serviceRegistrations.add(bundleContext.registerService(Subscriber.class, dataModel.getInstance(FirmwareCampaignHandler.class), new Hashtable<>()));
    }

    public void deactivate() {
        serviceRegistrations.forEach(ServiceRegistration::unregister);
        serviceRegistrations.clear();
    }

    void postEvent(EventType eventType, Object source){
        eventService.postEvent(eventType.topic(), source);
    }

    @Override
    public FirmwareVersion getFirmwareVersion(Map<String, Object> properties) {
        Optional<DeviceMessageSpec> firmwareMessageSpec = firmwareService.defaultFirmwareVersionSpec();
        List<PropInfo> propInfos = new ArrayList<>();
        properties.forEach((key, value) -> propInfos.add(new PropInfo(key, value)));
        if (firmwareMessageSpec.isPresent()) {
            Optional<PropertySpec> firmwareVersionPropertySpec = firmwareMessageSpec.get()
                    .getPropertySpecs()
                    .stream()
                    .filter(propertySpec -> propertySpec.getValueFactory().getValueType().equals(BaseFirmwareVersion.class))
                    .findAny();
            if (firmwareVersionPropertySpec.isPresent()) {
                return (FirmwareVersion) propInfos.stream()
                        .filter(property -> property.key.equals(firmwareVersionPropertySpec.get().getName()))
                        .findFirst()
                        .map(property -> firmwareVersionPropertySpec.get().getValueFactory().fromStringValue(property.value))
                        .orElse(null);
            }
        }
        return null;
    }

    class PropInfo {
        String key;
        String value;

        public PropInfo(String key, Object value) {
            this.key = key;
            this.value = value instanceof String ? (String) value : value instanceof Boolean ? Boolean.toString((Boolean) value) : value instanceof Long ? Long.toString((Long) value) : value != null ? Integer
                    .toString((Integer) value) : "";
        }
    }

}
