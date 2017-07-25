/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.rest.DeviceMessageStatusTranslationKeys;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.TrackingCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public DeviceMessageInfo asInfo(DeviceMessage deviceMessage, UriInfo uriInfo) {
        Device device = (Device) deviceMessage.getDevice();
        DeviceMessageInfo info = getBaseInfo(deviceMessage, uriInfo, device);
        info.userCanAdministrate = deviceMessageService.canUserAdministrateDeviceMessage(device.getDeviceConfiguration(), deviceMessage.getDeviceMessageId());
        if (EnumSet.of(DeviceMessageStatus.PENDING, DeviceMessageStatus.WAITING).contains(deviceMessage.getStatus()) && info.willBePickedUpByComTask==null) {
            info.willBePickedUpByComTask = this.deviceMessageService.willDeviceMessageBePickedUpByComTask(device, deviceMessage);
        }

        return info;
    }

    private DeviceMessageInfo getBaseInfo(DeviceMessage deviceMessage, UriInfo uriInfo, Device device) {
        DeviceMessageInfo info = new DeviceMessageInfo();
        info.id = deviceMessage.getId();
        info.trackingIdAndName = new IdWithNameInfo(deviceMessage.getTrackingId(), "");
        if (deviceMessage.getTrackingCategory() != null) {
            info.trackingCategory = new DeviceMessageInfo.TrackingCategoryInfo();
            info.trackingCategory.id = deviceMessage.getTrackingCategory().getKey();
            info.trackingCategory.name = thesaurus.getFormat(deviceMessage.getTrackingCategory()).format();
            info.trackingCategory.activeLink = isActive(deviceMessage.getTrackingId(), deviceMessage.getTrackingCategory(), info);
        }
        info.deviceConfiguration = new IdWithNameInfo(((Device)deviceMessage.getDevice()).getDeviceConfiguration());
        info.deviceType = new IdWithNameInfo(((Device)deviceMessage.getDevice()).getDeviceType());

        info.messageSpecification = new DeviceMessageSpecInfo();
        info.messageSpecification.id = deviceMessage.getSpecification().getId().name();
        info.messageSpecification.name = deviceMessage.getSpecification().getName();

        info.category = deviceMessage.getSpecification().getCategory().getName();
        info.status = new DeviceMessageInfo.StatusInfo();
        info.status.value = MESSAGE_STATUS_ADAPTER.marshal(deviceMessage.getStatus());
        info.status.displayValue = DeviceMessageStatusTranslationKeys.translationFor(deviceMessage.getStatus(), thesaurus);
        info.creationDate = deviceMessage.getCreationDate();
        info.releaseDate = deviceMessage.getReleaseDate();
        info.sentDate = deviceMessage.getSentDate().orElse(null);
        info.user = deviceMessage.getUser();
        info.errorMessage = deviceMessage.getProtocolInfo();

        if (EnumSet.of(DeviceMessageStatus.PENDING, DeviceMessageStatus.WAITING).contains(deviceMessage.getStatus())) {
            info.willBePickedUpByPlannedComTask = this.deviceMessageService.willDeviceMessageBePickedUpByPlannedComTask(device, deviceMessage);
            if (info.willBePickedUpByPlannedComTask) {
                info.willBePickedUpByComTask = true; // shortcut
            }
        }

        ComTask comTaskForDeviceMessage = deviceMessageService.getPreferredComTask(device, deviceMessage);

        if (comTaskForDeviceMessage!=null) {
            info.preferredComTask = new IdWithNameInfo(comTaskForDeviceMessage);
        }
        info.properties = new ArrayList<>();

        TypedProperties typedProperties = TypedProperties.empty();
        deviceMessage.getAttributes().stream().forEach(attribute->typedProperties.setProperty(attribute.getName(), attribute.getValue()));
        mdcPropertyUtils.convertPropertySpecsToPropertyInfos(uriInfo,
                deviceMessage.getAttributes().stream()
                        .map(DeviceMessageAttribute.class::cast)        //Downcast to Connexo DeviceMessageAttribute
                        .map(DeviceMessageAttribute::getSpecification).collect(toList()),
                typedProperties,
                info.properties
        );

        info.version = deviceMessage.getVersion();
        info.parent = new VersionInfo<>(device.getName(), device.getVersion());
        return info;
    }

    public List<DeviceMessageInfo> asInfo(Collection<DeviceMessage> deviceMessages, UriInfo uriInfo) {
        final Map<Long, Map<DeviceMessageId, Boolean>> userCanAdministrateCache = new HashMap<>();
        final Map<Long, Map<Integer, Boolean>> willBePickedUpByComTaskCache = new HashMap<>();
        final List<DeviceMessageInfo> infos = new ArrayList<>();
        for (DeviceMessage deviceMessage : deviceMessages) {
            Device device = (Device) deviceMessage.getDevice();
            DeviceMessageInfo info = getBaseInfo(deviceMessage, uriInfo, device);
            info.userCanAdministrate = getUserCanAdministrateFromCache(userCanAdministrateCache, deviceMessage, device);
            if (EnumSet.of(DeviceMessageStatus.PENDING, DeviceMessageStatus.WAITING).contains(deviceMessage.getStatus()) && info.willBePickedUpByComTask==null) {
                info.willBePickedUpByComTask = getWillBePickedUpByComTaskFromCache(willBePickedUpByComTaskCache, deviceMessage, device);
            }
            infos.add(info);
        }
        return infos;
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
                    if(serviceCall.isPresent()) {
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
