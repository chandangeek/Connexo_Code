package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.DeviceMessageStatusTranslationKeys;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.TrackingCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.MessagesTask;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
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

    @Inject
    public DeviceMessageInfoFactory(Thesaurus thesaurus, MdcPropertyUtils mdcPropertyUtils, ServiceCallService serviceCallService) {
        this.thesaurus = thesaurus;
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.serviceCallService = serviceCallService;
    }

    public DeviceMessageInfo asInfo(DeviceMessage<?> deviceMessage) {
        DeviceMessageInfo info = new DeviceMessageInfo();
        info.id = deviceMessage.getId();
        info.trackingIdAndName = new IdWithNameInfo(deviceMessage.getTrackingId(), "");
        if (deviceMessage.getTrackingCategory() != null) {
            info.trackingCategory = new DeviceMessageInfo.TrackingCategoryInfo();
            info.trackingCategory.id = deviceMessage.getTrackingCategory().getKey();
            info.trackingCategory.name = thesaurus.getFormat(deviceMessage.getTrackingCategory()).format();
            info.trackingCategory.activeLink = isActive(deviceMessage.getTrackingId(), deviceMessage.getTrackingCategory(), info);
        }
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

        Device device = (Device) deviceMessage.getDevice();
        if (EnumSet.of(DeviceMessageStatus.PENDING, DeviceMessageStatus.WAITING).contains(deviceMessage.getStatus())) {
            info.willBePickedUpByPlannedComTask = device.getComTaskExecutions().stream().
                    filter(cte-> !cte.isOnHold()).
                    flatMap(cte -> cte.getComTasks().stream()).
                    flatMap(comTask -> comTask.getProtocolTasks().stream()).
                    filter(task -> task instanceof MessagesTask).
                    flatMap(task -> ((MessagesTask) task).getDeviceMessageCategories().stream()).
                    anyMatch(category -> category.getId() == deviceMessage.getSpecification().getCategory().getId());
            if (info.willBePickedUpByPlannedComTask) {
                info.willBePickedUpByComTask = true; // shortcut
            } else {
                info.willBePickedUpByComTask = device.getDeviceConfiguration().
                        getComTaskEnablements().stream().
                        map(ComTaskEnablement::getComTask).
                        flatMap(comTask -> comTask.getProtocolTasks().stream()).
                        filter(task -> task instanceof MessagesTask).
                        flatMap(task -> ((MessagesTask) task).getDeviceMessageCategories().stream()).
                        anyMatch(category -> category.getId() == deviceMessage.getSpecification().getCategory().getId());
            }
        }

        ComTask comTaskForDeviceMessage = getPreferredComTask(device, deviceMessage);

        if (comTaskForDeviceMessage!=null) {
            info.preferredComTask = new IdWithNameInfo(comTaskForDeviceMessage);
        }
        info.properties = new ArrayList<>();

        TypedProperties typedProperties = TypedProperties.empty();
        deviceMessage.getAttributes().stream().forEach(attribute->typedProperties.setProperty(attribute.getName(), attribute.getValue()));
        mdcPropertyUtils.convertPropertySpecsToPropertyInfos(null,
                deviceMessage.getAttributes().stream().map(DeviceMessageAttribute::getSpecification).collect(toList()),
                typedProperties,
                info.properties
        );

        info.version = deviceMessage.getVersion();
        info.parent = new VersionInfo<>(device.getmRID(), device.getVersion());
        return info;
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

    private ComTask getPreferredComTask(Device device, DeviceMessage<?> deviceMessage) {
        return device.getComTaskExecutions().stream().
            filter(cte -> cte.isAdHoc() && cte.isOnHold()).
            flatMap(cte -> cte.getComTasks().stream()).
            filter(comTask -> comTask.getProtocolTasks().stream().
                    filter(task -> task instanceof MessagesTask).
                    flatMap(task -> ((MessagesTask) task).getDeviceMessageCategories().stream()).
                    anyMatch(category -> category.getId() == deviceMessage.getSpecification().getCategory().getId())).
                findFirst(). // An adHoc comTask that has already been executed (nextExecTimestamp==null)
            orElse(device.
                getDeviceConfiguration().
                getComTaskEnablements().stream().
                map(ComTaskEnablement::getComTask).
                filter(ct -> device.getComTaskExecutions().stream().
                        flatMap(cte -> cte.getComTasks().stream()).
                        noneMatch(comTask -> comTask.getId() == ct.getId())).
                filter(comTask -> comTask.getProtocolTasks().stream().
                        filter(task -> task instanceof MessagesTask).
                        flatMap(task -> ((MessagesTask) task).getDeviceMessageCategories().stream()).
                        anyMatch(category -> category.getId() == deviceMessage.getSpecification().getCategory().getId())).
                findFirst(). // A Dangling ComTask -> There is no ComTaskExecution yet but the enabled comTask supports the device message category
            orElse(device.
                getComTaskExecutions().stream().
                sorted(Comparator.comparing(ComTaskExecution::isAdHoc).thenComparing(ComTaskExecution::isScheduledManually).thenComparing(ComTaskExecution::isOnHold).reversed()).
                flatMap(cte -> cte.getComTasks().stream()).
                filter(comTask -> comTask.getProtocolTasks().stream().
                        filter(task -> task instanceof MessagesTask).
                        flatMap(task -> ((MessagesTask) task).getDeviceMessageCategories().stream()).
                        anyMatch(category -> category.getId() == deviceMessage.getSpecification().getCategory().getId())).
                findFirst().
                orElse(null)));
    }

}
