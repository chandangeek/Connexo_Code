/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.common.protocol.TrackingCategory;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.rest.DeviceMessageStatusTranslationKeys;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 10/22/14.
 */
public class DeviceMessageInfoFactory {
    private static final MessageStatusAdapter MESSAGE_STATUS_ADAPTER = new MessageStatusAdapter();

    private final Thesaurus thesaurus;
    private final MdcPropertyUtils mdcPropertyUtils;
    private final ServiceCallService serviceCallService;
    private final DeviceMessageService deviceMessageService;

    @Inject
    public DeviceMessageInfoFactory(Thesaurus thesaurus, MdcPropertyUtils mdcPropertyUtils, ServiceCallService serviceCallService, DeviceMessageService deviceMessageService) {
        this.thesaurus = thesaurus;
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.serviceCallService = serviceCallService;
        this.deviceMessageService = deviceMessageService;
    }

    public DeviceMessageInfo asFullInfo(DeviceMessage deviceMessage, UriInfo uriInfo) {
        Device device = deviceMessage.getDevice();
        DeviceMessageInfo info = getBaseInfo(deviceMessage, uriInfo);
        ComTask comTaskForDeviceMessage = deviceMessageService.getPreferredComTask(device, deviceMessage);
        if (comTaskForDeviceMessage != null) {
            info.preferredComTask = new IdWithNameInfo(comTaskForDeviceMessage);
        }
        info.userCanAdministrate = deviceMessageService.canUserAdministrateDeviceMessage(device.getDeviceConfiguration(), deviceMessage.getDeviceMessageId());
        if (EnumSet.of(DeviceMessageStatus.PENDING, DeviceMessageStatus.WAITING).contains(deviceMessage.getStatus())) {
            info.willBePickedUpByPlannedComTask = this.deviceMessageService.willDeviceMessageBePickedUpByPlannedComTask(device, deviceMessage);
            if (info.willBePickedUpByPlannedComTask) {
                info.willBePickedUpByComTask = true; // shortcut
            }
        }
        if (EnumSet.of(DeviceMessageStatus.PENDING, DeviceMessageStatus.WAITING).contains(deviceMessage.getStatus()) && info.willBePickedUpByComTask == null) {
            info.willBePickedUpByComTask = this.deviceMessageService.willDeviceMessageBePickedUpByComTask(device, deviceMessage);
        }
        return info;
    }

    public List<DeviceMessageInfo> asFullInfoWithCache(Collection<DeviceMessage> deviceMessages, UriInfo uriInfo) {
        Map<Integer, ComTask> preferredComTaskCache = new HashMap<>();
        Map<DeviceMessageId, Boolean> userCanAdministrateCache = new HashMap<>();
        Map<Integer, Boolean> willDeviceMessageBePickedUpByComTaskCache = new HashMap<>();
        Map<Integer, Boolean> willBePickedUpByPlannedComTaskCache = new HashMap<>();

        return deviceMessages.stream().
                map(deviceMessage -> {
                    Device device = deviceMessage.getDevice();
                    DeviceMessageInfo info = getBaseInfo(deviceMessage, uriInfo);
                    ComTask comTaskForDeviceMessage = preferredComTaskCache.computeIfAbsent(deviceMessage.getSpecification().getCategory().getId(),
                            key -> deviceMessageService.getPreferredComTask(device, deviceMessage));
                    if (comTaskForDeviceMessage != null) {
                        info.preferredComTask = new IdWithNameInfo(comTaskForDeviceMessage);
                    }

                    info.userCanAdministrate = userCanAdministrateCache.computeIfAbsent(deviceMessage.getDeviceMessageId(),
                            key -> deviceMessageService.canUserAdministrateDeviceMessage(device.getDeviceConfiguration(), deviceMessage.getDeviceMessageId()));

                    if (EnumSet.of(DeviceMessageStatus.PENDING, DeviceMessageStatus.WAITING).contains(deviceMessage.getStatus())) {
                        info.willBePickedUpByPlannedComTask = willBePickedUpByPlannedComTaskCache.computeIfAbsent(deviceMessage.getSpecification().getCategory().getId(),
                                key -> this.deviceMessageService.willDeviceMessageBePickedUpByPlannedComTask(device, deviceMessage));
                        if (info.willBePickedUpByPlannedComTask) {
                            info.willBePickedUpByComTask = true; // shortcut
                        }
                    }

                    if (EnumSet.of(DeviceMessageStatus.PENDING, DeviceMessageStatus.WAITING).contains(deviceMessage.getStatus()) && info.willBePickedUpByComTask == null) {
                        info.willBePickedUpByComTask = willDeviceMessageBePickedUpByComTaskCache.computeIfAbsent(deviceMessage.getSpecification().getCategory().getId(),
                                key -> this.deviceMessageService.willDeviceMessageBePickedUpByComTask(device, deviceMessage));
                    }
                    return info;
                }).
                collect(toList());
    }

    public List<DeviceMessageInfo> asFasterInfo(Collection<DeviceMessage> deviceMessages) {
        final Map<Long, Map<DeviceMessageId, Boolean>> userCanAdministrateCache = new HashMap<>();
        final Map<Long, Map<Integer, Boolean>> willBePickedUpByComTaskCache = new HashMap<>();
        final List<DeviceMessageInfo> infos = new ArrayList<>();
        for (DeviceMessage deviceMessage : deviceMessages) {
            Device device = deviceMessage.getDevice();
            DeviceMessageInfo info = getSimpleInfo(deviceMessage);
            info.userCanAdministrate = getUserCanAdministrateFromCache(userCanAdministrateCache, deviceMessage, device);
            if (EnumSet.of(DeviceMessageStatus.PENDING, DeviceMessageStatus.WAITING).contains(deviceMessage.getStatus()) && info.willBePickedUpByComTask == null) {
                info.willBePickedUpByComTask = getWillBePickedUpByComTaskFromCache(willBePickedUpByComTaskCache, deviceMessage, device);
            }
            infos.add(info);
        }
        return infos;
    }

    public DeviceMessageInfo getSimpleInfo(DeviceMessage deviceMessage) {
        DeviceMessageInfo info = new DeviceMessageInfo();
        info.id = deviceMessage.getId();
        Device device = deviceMessage.getDevice();
        DeviceMessageSpec specification = deviceMessage.getSpecification();

        info.status = new DeviceMessageInfo.StatusInfo();
        info.status.value = MESSAGE_STATUS_ADAPTER.marshal(deviceMessage.getStatus());
        info.status.displayValue = DeviceMessageStatusTranslationKeys.translationFor(deviceMessage.getStatus(), thesaurus);

        info.category = specification.getCategory().getName();

        info.releaseDate = deviceMessage.getReleaseDate();
        info.sentDate = deviceMessage.getSentDate().orElse(null);

        info.messageSpecification = new DeviceMessageSpecInfo();
        info.messageSpecification.id = specification.getId().name();
        info.messageSpecification.name = specification.getName();

        info.user = deviceMessage.getUser();

        info.parent = new VersionInfo<>(device.getName(), device.getVersion());
        info.version = deviceMessage.getVersion();
        ComTask comTaskForDeviceMessage = deviceMessageService.getPreferredComTask(device, deviceMessage);
        if (comTaskForDeviceMessage != null) {
            info.preferredComTask = new IdWithNameInfo(comTaskForDeviceMessage);
        }
        return info;
    }

    private DeviceMessageInfo getBaseInfo(DeviceMessage deviceMessage, UriInfo uriInfo) {
        DeviceMessageInfo info = getSimpleInfo(deviceMessage);
        info.trackingIdAndName = new IdWithNameInfo(deviceMessage.getTrackingId(), "");
        if (deviceMessage.getTrackingCategory() != null) {
            info.trackingCategory = new DeviceMessageInfo.TrackingCategoryInfo();
            info.trackingCategory.id = deviceMessage.getTrackingCategory().getKey();
            info.trackingCategory.name = thesaurus.getFormat(deviceMessage.getTrackingCategory()).format();
            info.trackingCategory.activeLink = isActive(deviceMessage.getTrackingId(), deviceMessage.getTrackingCategory(), info);
        }
        info.deviceConfiguration = new IdWithNameInfo((deviceMessage.getDevice()).getDeviceConfiguration());
        info.deviceType = new IdWithNameInfo((deviceMessage.getDevice()).getDeviceType());

        info.creationDate = deviceMessage.getCreationDate();
        info.errorMessage = deviceMessage.getProtocolInfo();
        info.properties = new ArrayList<>();

        TypedProperties typedProperties = TypedProperties.empty();
        deviceMessage.getAttributes().stream().forEach(attribute -> typedProperties.setProperty(attribute.getName(), attribute.getValue()));
        mdcPropertyUtils.convertPropertySpecsToPropertyInfos(uriInfo,
                deviceMessage.getAttributes().stream()
                        .map(DeviceMessageAttribute.class::cast)        //Downcast to Connexo DeviceMessageAttribute
                        .map(DeviceMessageAttribute::getSpecification)
                        .filter(Objects::nonNull)
                        .collect(toList()),
                typedProperties,
                info.properties
        );
        if (typedProperties.size() > 0 && info.properties.size() < typedProperties.size()) {
            HashMap<String, PropertyInfo> props = new HashMap<>();
            info.properties.stream().forEach(p -> props.put(p.key, p));
            typedProperties.stream().filter(e -> Objects.isNull(props.get(e.getKey())))
                    .forEach(entry -> props.put(entry.getKey(), getArchivedProperty(entry)));
            info.properties.clear();
            deviceMessage.getAttributes().forEach(a -> info.properties.add((PropertyInfo) props.get(a.getName())));
        }

        return info;
    }

    private PropertyInfo getArchivedProperty(Map.Entry<String, Object> entry) {
        String name = thesaurus.getString(entry.getKey(), null);
        String key = entry.getKey();
        String decription = "Description for " + key;
        PropertyValueInfo propertyValueInfo = new PropertyValueInfo(entry.getValue(), null, null, null);
        PropertyTypeInfo propertyTypeInfo = new PropertyTypeInfo();
        propertyTypeInfo.simplePropertyType = SimplePropertyType.UNKNOWN;
        return new PropertyInfo(name, key, decription, propertyValueInfo, propertyTypeInfo, true);
    }

    private Boolean getUserCanAdministrateFromCache(Map<Long, Map<DeviceMessageId, Boolean>> userCanAdministrateCache, DeviceMessage deviceMessage, Device device) {
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        Map<DeviceMessageId, Boolean> deviceMessageCache = userCanAdministrateCache.computeIfAbsent(deviceConfiguration
                .getId(), x -> new HashMap<>());
        DeviceMessageId deviceMessageId = deviceMessage.getDeviceMessageId();
        return deviceMessageCache.computeIfAbsent(deviceMessageId,
                deviceMessageIdx -> deviceMessageService.canUserAdministrateDeviceMessage(deviceConfiguration, deviceMessageId));
    }

    private Boolean getWillBePickedUpByComTaskFromCache(Map<Long, Map<Integer, Boolean>> willBePickedUpByComTaskCache, DeviceMessage deviceMessage, Device device) {
        Map<Integer, Boolean> deviceMessageCache = willBePickedUpByComTaskCache.computeIfAbsent(device.getDeviceConfiguration()
                .getId(), x -> new HashMap<>());
        return deviceMessageCache.computeIfAbsent(deviceMessage.getSpecification().getCategory().getId(),
                deviceMessageId -> this.deviceMessageService.willDeviceMessageBePickedUpByComTask(device, deviceMessage));
    }

    private boolean isActive(String trackingId, TrackingCategory trackingCategory, DeviceMessageInfo info) {
        switch (trackingCategory) {
            case serviceCall:
                try {
                    long id = Long.parseLong(trackingId);
                    Optional<ServiceCall> serviceCall = serviceCallService.getServiceCall(id);
                    if (serviceCall.isPresent()) {
                        info.trackingIdAndName = new IdWithNameInfo(serviceCall.get().getId(), serviceCall.get().getNumber());
                    }
                    return serviceCall.isPresent();
                } catch (Exception e) {
                    throw new LocalizedFieldValidationException(MessageSeeds.INVALID_TRACKING_ID, "trackingIdAndName");
                }
            case manual:
            default:
                return false;
        }
    }


}
