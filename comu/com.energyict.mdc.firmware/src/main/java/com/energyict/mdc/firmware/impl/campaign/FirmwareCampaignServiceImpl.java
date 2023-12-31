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
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
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
import com.energyict.mdc.protocol.api.firmware.BaseFirmwareVersion;
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
import java.util.stream.Stream;

public class FirmwareCampaignServiceImpl implements FirmwareCampaignService {
    private final FirmwareServiceImpl firmwareService;
    private final DataModel dataModel;
    private final DeviceService deviceService;
    private final ServiceCallService serviceCallService;
    private final OrmService ormService;
    private final Thesaurus thesaurus;
    private final MeteringGroupsService meteringGroupsService;
    private final List<ServiceRegistration> serviceRegistrations = new ArrayList<>();
    private final RegisteredCustomPropertySet registeredCustomPropertySet;
    private final EventService eventService;
    private final TaskService taskService;
    private final DeviceMessageService deviceMessageService;

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
        this.deviceMessageService = dataModel.getInstance(DeviceMessageService.class);
    }

    public ServiceCall createServiceCallAndTransition(FirmwareCampaignDomainExtension campaign) {
        return getServiceCallType(ServiceCallTypes.FIRMWARE_CAMPAIGN)
                .map(serviceCallType -> createServiceCall(serviceCallType, campaign))
                .orElseThrow(() -> new IllegalStateException("Service call type FIRMWARE_CAMPAIGN not found."));
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
    public Optional<FirmwareCampaign> getFirmwareCampaignById(long id) {
        return ormService.getDataModel(FirmwareCampaignPersistenceSupport.COMPONENT_NAME).get()
                .mapper(FirmwareCampaignDomainExtension.class)
                .getOptional(id, registeredCustomPropertySet.getId())
                .map(FirmwareCampaign.class::cast);
    }

    @Override
    public Optional<FirmwareCampaign> getCampaignByName(String name) {
        return streamAllCampaigns().filter(Where.where("name").isEqualTo(name)).findAny().map(FirmwareCampaign.class::cast);
    }

    @Override
    public Optional<FirmwareCampaign> findAndLockFirmwareCampaignByIdAndVersion(long id, long version) {
        return ormService.getDataModel(FirmwareCampaignPersistenceSupport.COMPONENT_NAME).get()
                .mapper(FirmwareCampaignDomainExtension.class)
                .lockObjectIfVersion(version, id, registeredCustomPropertySet.getId())
                .map(FirmwareCampaign.class::cast);
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
                    MessageSeeds messageSeeds = createChildServiceCall(serviceCall, device, campaign);
                    numberOfDevices.compute(messageSeeds, (key, value) -> value == null ? 1 : value + 1);
                });
            } else {
                serviceCall.log(LogLevel.INFO, thesaurus.getFormat(MessageSeeds.DEVICES_WITH_GROUP_AND_TYPE_NOT_FOUND)
                        .format(campaign.getDeviceGroup(), campaign.getDeviceType().getName()));
            }
            int notAddedDevicesBecauseDifferentType = devicesByGroup.size() - devicesByGroupAndType.size();
            if (notAddedDevicesBecauseDifferentType == 1) {
                serviceCall.log(LogLevel.INFO, thesaurus.getFormat(MessageSeeds.DEVICE_HASNT_ADDED_BECAUSE_DIFFERENT_TYPE).format());
            } else if (notAddedDevicesBecauseDifferentType > 0) {
                serviceCall.log(LogLevel.INFO, thesaurus.getFormat(MessageSeeds.DEVICES_HAVENT_ADDED_BECAUSE_DIFFERENT_TYPE).format(notAddedDevicesBecauseDifferentType));
            }
            Integer notAdded = numberOfDevices.get(MessageSeeds.DEVICES_HAVENT_ADDED_BECAUSE_PART_OTHER_CAMPAIGN);
            if (notAdded != null) {
                if (notAdded == 1) {
                    serviceCall.log(LogLevel.INFO, thesaurus.getFormat(MessageSeeds.DEVICE_HASNT_ADDED_BECAUSE_PART_OTHER_CAMPAIGN).format());
                } else {
                    serviceCall.log(LogLevel.INFO, thesaurus.getFormat(MessageSeeds.DEVICES_HAVENT_ADDED_BECAUSE_PART_OTHER_CAMPAIGN).format(notAdded));
                }
            }
            notAdded = numberOfDevices.get(MessageSeeds.DEVICES_HAVENT_ADDED_BECAUSE_HAVE_THIS_FIRMWARE_VERSION);
            if (notAdded != null) {
                if (notAdded == 1) {
                    serviceCall.log(LogLevel.INFO, thesaurus.getFormat(MessageSeeds.DEVICE_HASNT_ADDED_BECAUSE_HAVE_THIS_FIRMWARE_VERSION).format());
                } else {
                    serviceCall.log(LogLevel.INFO, thesaurus.getFormat(MessageSeeds.DEVICES_HAVENT_ADDED_BECAUSE_HAVE_THIS_FIRMWARE_VERSION).format(notAdded));
                }
            }
            if (numberOfDevices.get(MessageSeeds.DEVICE_WAS_ADDED) == null) {
                serviceCall.transitionWithLockIfPossible(DefaultState.CANCELLED);
                serviceCall.log(LogLevel.INFO, thesaurus.getSimpleFormat(MessageSeeds.CAMPAIGN_WAS_CANCELED_BECAUSE_DIDNT_RECEIVE_DEVICES).format());
            }
        });
    }

    private List<Device> getDevicesByGroup(String deviceGroup) {
        EndDeviceGroup endDeviceGroup = meteringGroupsService.findEndDeviceGroupByName(deviceGroup)
                .orElseThrow(() -> new FirmwareCampaignException(thesaurus, MessageSeeds.DEVICE_GROUP_ISNT_FOUND, deviceGroup));
        return deviceService.deviceQuery().select(ListOperator.IN.contains(endDeviceGroup.toSubQuery("id"), "meter"));
    }

    private MessageSeeds createChildServiceCall(ServiceCall parent, Device device, FirmwareCampaignDomainExtension campaign) {
        if (!findActiveFirmwareItemByDevice(device).isPresent()) {
            if (campaign.isWithUniqueFirmwareVersion() && firmwareService.getActiveFirmwareVersion(device, campaign.getFirmwareType())
                    .filter(firmwareVersion -> firmwareVersion.getFirmwareVersion().equals(campaign.getFirmwareVersion()))
                    .isPresent()) {
                return MessageSeeds.DEVICES_HAVENT_ADDED_BECAUSE_HAVE_THIS_FIRMWARE_VERSION;
            }
            ServiceCallType serviceCallType = getServiceCallTypeOrThrowException(ServiceCallTypes.FIRMWARE_CAMPAIGN_ITEM);
            FirmwareCampaignItemDomainExtension firmwareCampaignItemDomainExtension = dataModel.getInstance(FirmwareCampaignItemDomainExtension.class);
            firmwareCampaignItemDomainExtension.setDevice(device);
            firmwareCampaignItemDomainExtension.setParent(parent);
            ServiceCall serviceCall = parent.newChildCall(serviceCallType).extendedWith(firmwareCampaignItemDomainExtension).targetObject(device).create();
            postEvent(EventType.DEVICE_IN_FIRMWARE_CAMPAIGN_CREATED, firmwareCampaignItemDomainExtension);
            serviceCall.requestTransition(DefaultState.PENDING);
            return MessageSeeds.DEVICE_WAS_ADDED;
        } else {
            return MessageSeeds.DEVICES_HAVENT_ADDED_BECAUSE_PART_OTHER_CAMPAIGN;
        }
    }

    public ServiceCallType getServiceCallTypeOrThrowException(ServiceCallTypes serviceCallType) {
        return serviceCallService.findServiceCallType(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())
                .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.COULDNT_FIND_SERVICE_CALL_TYPE)
                        .format(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())));
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
        return findActiveFirmwareItemByDevice(comTaskExecution.getDevice())
                .map(deviceInFirmwareCampaign -> deviceInFirmwareCampaign.getParent().getExtension(FirmwareCampaignDomainExtension.class).get());
    }

    @Override
    public Optional<FirmwareCampaignItemDomainExtension> findActiveFirmwareItemByDevice(Device device) {
        return streamDevicesInCampaigns().join(ServiceCall.class).join(State.class)
                .filter(Where.where("device").isEqualTo(device))
                .filter(Where.where("serviceCall.state.name").in(DefaultState.openStateKeys()))
                .findAny();
    }

    @Override
    public Optional<FirmwareCampaignItemDomainExtension> findFirmwareItem(long campaignId, Device device) {
        // TODO refactor campaign item to keep the link to parent campaign, not parent service call, in order to make it possible filtering the required items on query level
        return getFirmwareCampaignById(campaignId)
                .map(FirmwareCampaign::getServiceCall)
                .map(ServiceCall::findChildren)
                .map(Finder::stream)
                .orElseGet(Stream::empty)
                .map(sc -> sc.getExtension(FirmwareCampaignItemDomainExtension.class))
                .flatMap(Functions.asStream())
                .filter(item -> item.getDevice().equals(device))
                .findAny();
    }

    @Override
    public QueryStream<FirmwareCampaignItemDomainExtension> streamDevicesInCampaigns() {
        return ormService.getDataModel(FirmwareCampaignItemPersistenceSupport.COMPONENT_NAME).get().stream(FirmwareCampaignItemDomainExtension.class);
    }

    @Override
    public QueryStream<FirmwareCampaignDomainExtension> streamAllCampaigns() {
        return ormService.getDataModel(FirmwareCampaignPersistenceSupport.COMPONENT_NAME).get().stream(FirmwareCampaignDomainExtension.class);
    }

    @Override
    public List<FirmwareCampaign> findFirmwareCampaigns(DeviceType deviceType) {
        return streamAllCampaigns().filter(Where.where("deviceType").isEqualTo(deviceType)).collect(Collectors.toList());
    }

    @Override
    public List<DeviceInFirmwareCampaign> findFirmwareCampaignItems(Device device) {
        return streamDevicesInCampaigns().filter(Where.where("device").isEqualTo(device)).collect(Collectors.toList());
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

    void postEvent(EventType eventType, Object source) {
        eventService.postEvent(eventType.topic(), source);
    }

    @Override
    public FirmwareVersion getFirmwareVersion(Map<String, Object> properties) {
        Optional<DeviceMessageSpec> firmwareMessageSpec = firmwareService.defaultFirmwareVersionSpec();
        if (firmwareMessageSpec.isPresent()) {
            Optional<PropertySpec> firmwareVersionPropertySpec = firmwareMessageSpec.get()
                    .getPropertySpecs()
                    .stream()
                    .filter(propertySpec -> BaseFirmwareVersion.class.isAssignableFrom(propertySpec.getValueFactory().getValueType()))
                    .findAny();
            if (firmwareVersionPropertySpec.isPresent()) {
                Object value = properties.get(firmwareVersionPropertySpec.get().getName());
                if (value != null) {
                    Object firmwareVersion = firmwareVersionPropertySpec.get().getValueFactory().fromStringValue(String.valueOf(value));
                    if (firmwareVersion instanceof FirmwareVersion) {
                        return (FirmwareVersion) firmwareVersion;
                    }
                }
            }
        }
        return null;
    }
}
