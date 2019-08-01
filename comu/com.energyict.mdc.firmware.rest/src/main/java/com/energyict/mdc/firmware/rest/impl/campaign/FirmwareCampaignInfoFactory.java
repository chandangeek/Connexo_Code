/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl.campaign;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.time.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaignBuilder;
import com.energyict.mdc.firmware.FirmwareCampaignService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.rest.impl.FirmwareMessageInfoFactory;
import com.energyict.mdc.firmware.rest.impl.FirmwareTypeInfo;
import com.energyict.mdc.firmware.rest.impl.FirmwareVersionInfoFactory;
import com.energyict.mdc.firmware.rest.impl.IdWithLocalizedValue;
import com.energyict.mdc.firmware.rest.impl.ManagementOptionInfo;
import com.energyict.mdc.firmware.rest.impl.MessageSeeds;
import com.energyict.mdc.firmware.rest.impl.ResourceHelper;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.energyict.mdc.firmware.rest.impl.campaign.StatusInfoFactory.getCampaignStatus;
import static com.energyict.mdc.firmware.rest.impl.campaign.StatusInfoFactory.getDeviceStatus;

public class FirmwareCampaignInfoFactory {
    private final Thesaurus thesaurus;
    private final ResourceHelper resourceHelper;
    private final FirmwareCampaignService firmwareCampaignService;
    private final FirmwareService firmwareService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final FirmwareVersionInfoFactory firmwareVersionFactory;
    private final FirmwareMessageInfoFactory firmwareMessageInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final Clock clock;

    @Inject
    public FirmwareCampaignInfoFactory(
            Thesaurus thesaurus,
            DeviceConfigurationService deviceConfigurationService,
            DeviceMessageSpecificationService deviceMessageSpecificationService,
            ResourceHelper resourceHelper,
            FirmwareVersionInfoFactory firmwareVersionFactory,
            FirmwareMessageInfoFactory firmwareMessageInfoFactory,
            ExceptionFactory exceptionFactory,
            Clock clock,
            FirmwareService firmwareService) {
        this.thesaurus = thesaurus;
        this.resourceHelper = resourceHelper;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.firmwareVersionFactory = firmwareVersionFactory;
        this.firmwareMessageInfoFactory = firmwareMessageInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.clock = clock;
        this.firmwareService = firmwareService;
        this.firmwareCampaignService = firmwareService.getFirmwareCampaignService();
    }

    public FirmwareCampaignInfo from(FirmwareCampaign campaign) {
        FirmwareCampaignInfo info = new FirmwareCampaignInfo();
        info.id = campaign.getId();
        info.name = campaign.getName();
        info.deviceType = new IdWithLocalizedValue<>(campaign.getDeviceType().getId(), campaign.getDeviceType().getName());
        info.deviceGroup = campaign.getDeviceGroup();
        info.timeBoundaryStart = campaign.getUploadPeriodStart();
        info.timeBoundaryEnd = campaign.getUploadPeriodEnd();
        info.firmwareType = new FirmwareTypeInfo(campaign.getFirmwareType(), thesaurus);
        info.validationTimeout = new TimeDurationInfo(campaign.getValidationTimeout());
        String managementOptionId = campaign.getFirmwareManagementOption().getId();
        info.managementOption = new ManagementOptionInfo(managementOptionId, thesaurus.getString(managementOptionId, managementOptionId));
        info.version = campaign.getVersion();
        info.calendarUploadComTask = new IdWithNameInfo(campaign.getFirmwareUploadComTaskId(),firmwareCampaignService.getComTaskById(campaign.getFirmwareUploadComTaskId()).getName());
        info.calendarUploadConnectionStrategy = campaign.getFirmwareUploadConnectionStrategy().isPresent()?new IdWithNameInfo(campaign.getFirmwareUploadConnectionStrategy().get(), thesaurus.getString(campaign.getFirmwareUploadConnectionStrategy().get().name(), campaign.getFirmwareUploadConnectionStrategy().get().name())):null;
        info.validationComTask = new IdWithNameInfo(campaign.getValidationComTaskId(),firmwareCampaignService.getComTaskById(campaign.getValidationComTaskId()).getName());
        info.validationConnectionStrategy = campaign.getValidationConnectionStrategy().isPresent()?new IdWithNameInfo(campaign.getValidationConnectionStrategy().get(),thesaurus.getString(campaign.getValidationConnectionStrategy().get().name(), campaign.getValidationConnectionStrategy().get().name())):null;
        Optional<DeviceMessageSpec> firmwareMessageSpec = campaign.getFirmwareMessageSpec();
        if (firmwareMessageSpec.isPresent()) {
            info.firmwareVersion = campaign.getFirmwareVersion() != null ? firmwareVersionFactory.from(campaign.getFirmwareVersion()) : null;//may be todo else
            info.properties = firmwareMessageInfoFactory.getProperties(firmwareMessageSpec.get(), campaign.getDeviceType(), info.firmwareType.id.getType(), campaign.getProperties());
        }
        return info;
    }

    public FirmwareCampaignInfo getOverviewCampaignInfo(FirmwareCampaign campaign) {
        FirmwareCampaignInfo info = from(campaign);
        ServiceCall campaignsServiceCall = campaign.getServiceCall();
        info.startedOn = campaign.getStartedOn();
        info.finishedOn = campaign.getFinishedOn();
        info.status = getCampaignStatus(campaignsServiceCall.getState(), thesaurus);
        info.devices = new ArrayList<>();
        info.devices.add(new DevicesStatusAndQuantity(getDeviceStatus(DefaultState.SUCCESSFUL, thesaurus), 0L));
        info.devices.add(new DevicesStatusAndQuantity(getDeviceStatus(DefaultState.FAILED, thesaurus), 0L));
        info.devices.add(new DevicesStatusAndQuantity(getDeviceStatus(DefaultState.REJECTED, thesaurus), 0L));
        info.devices.add(new DevicesStatusAndQuantity(getDeviceStatus(DefaultState.ONGOING, thesaurus), 0L));
        info.devices.add(new DevicesStatusAndQuantity(getDeviceStatus(DefaultState.PENDING, thesaurus), 0L));
        info.devices.add(new DevicesStatusAndQuantity(getDeviceStatus(DefaultState.CANCELLED, thesaurus), 0L));
        campaign.getNumbersOfChildrenWithStatuses().forEach((deviceStatus, quantity) ->
                info.devices.stream()
                        .filter(devicesStatusAndQuantity -> devicesStatusAndQuantity.status.id.equals(deviceStatus.name()))
                        .findAny()
                        .ifPresent(devicesStatusAndQuantity -> devicesStatusAndQuantity.quantity = quantity));
        info.serviceCall = new IdWithNameInfo(campaignsServiceCall.getId(), campaignsServiceCall.getNumber());
        return info;
    }

    public FirmwareCampaign build(FirmwareCampaignInfo info) {
        Range<Instant> timeFrame = retrieveRealUploadRange(info);
        DeviceType deviceType = deviceConfigurationService.findDeviceType(((Number) info.deviceType.id).longValue())
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICETYPE_WITH_ID_ISNT_FOUND, info.deviceType.id));
        ProtocolSupportedFirmwareOptions managementOptions = ProtocolSupportedFirmwareOptions.from(info.managementOption.id)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.PROTOCOL_WITH_ID_ISNT_FOUND, info.managementOption.id));
        Long firmwareVersionId = info.getPropertyInfo(FirmwareMessageInfoFactory.PROPERTY_KEY_FIRMWARE_VERSION)
                .flatMap(resourceHelper::getPropertyInfoValueLong)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.FIRMWARE_VERSION_MISSING));
        FirmwareVersion firmwareVersion = resourceHelper.findFirmwareVersionByIdOrThrowException(firmwareVersionId);
        FirmwareCampaignBuilder firmwareCampaignBuilder = firmwareCampaignService
                .newFirmwareCampaign(info.name)
                .withDeviceType(deviceType)
                .withDeviceGroup(info.deviceGroup)
                .withFirmwareType(firmwareService.getFirmwareVersionById(firmwareVersionId).get().getFirmwareType())
                .withManagementOption(managementOptions)
                .withValidationTimeout(info.validationTimeout.asTimeDuration())
                .withFirmwareUploadComTaskId(((Number)info.calendarUploadComTask.id).longValue())
                .withValidationComTaskId(((Number)info.validationComTask.id).longValue())
                .withFirmwareUploadConnectionStrategy(info.calendarUploadConnectionStrategy==null?null:info.calendarUploadConnectionStrategy.name)
                .withValidationConnectionStrategy(info.validationConnectionStrategy==null?null:info.validationConnectionStrategy.name)
                .withUploadTimeBoundaries(timeFrame.lowerEndpoint(), timeFrame.upperEndpoint());
        DeviceMessageId firmwareMessageId = resourceHelper.findFirmwareMessageIdOrThrowException(deviceType, info.managementOption.id, firmwareVersion);
        String imageIdentifier = firmwareVersion.getImageIdentifier();
        if (deviceMessageSpecificationService.needsImageIdentifierAtFirmwareUpload(firmwareMessageId) && imageIdentifier != null) {
            info.getPropertyInfo(FirmwareMessageInfoFactory.PROPERTY_KEY_IMAGE_IDENTIFIER).ifPresent(x -> x.propertyValueInfo = new PropertyValueInfo<>(imageIdentifier, imageIdentifier, true));
        }
        info.getPropertyInfo(FirmwareMessageInfoFactory.PROPERTY_KEY_RESUME).ifPresent(x -> x.propertyValueInfo = new PropertyValueInfo<>(false, false, true));
        Map<String, Object> properties = new HashMap<>();
        info.properties.forEach(propertyInfo -> properties.put(propertyInfo.key, propertyInfo.propertyValueInfo.getValue()));
        Optional<DeviceMessageSpec> deviceMessageSpec = firmwareService.getFirmwareMessageSpec(deviceType, managementOptions,
                firmwareCampaignService.getFirmwareVersion(properties));
        if (deviceMessageSpec.isPresent()) {
            for (PropertySpec propertySpec : deviceMessageSpec.get().getPropertySpecs()) {
                Object propertyValue = firmwareMessageInfoFactory.findPropertyValue(propertySpec, info.properties);
                if (propertyValue != null) {
                    firmwareCampaignBuilder.addProperty(propertySpec, propertyValue);
                }
            }
        }
        return firmwareCampaignBuilder.create();
    }

    public Range<Instant> retrieveRealUploadRange(FirmwareCampaignInfo firmwareCampaignInfo) {
        Instant activationStart = firmwareCampaignInfo.timeBoundaryStart;
        Instant activationEnd = firmwareCampaignInfo.timeBoundaryEnd;
        if (isForToday(activationStart)) {
            if (activationStart.isAfter(activationEnd)) {
                activationEnd = getToday(clock).plusSeconds(getSecondsInDays(1)).plusSeconds(activationEnd.getEpochSecond());
            } else {
                activationEnd = getToday(clock).plusSeconds(activationEnd.getEpochSecond());
            }
            activationStart = getToday(clock).plusSeconds(activationStart.getEpochSecond());

        } else {
            if (activationStart.isAfter(activationEnd)) {
                activationEnd = getToday(clock).plusSeconds(getSecondsInDays(2)).plusSeconds(activationEnd.getEpochSecond());
            } else {
                activationEnd = getToday(clock).plusSeconds(getSecondsInDays(1)).plusSeconds(activationEnd.getEpochSecond());
            }
            activationStart = getToday(clock).plusSeconds(getSecondsInDays(1)).plusSeconds(activationStart.getEpochSecond());
        }
        return Range.closed(activationStart, activationEnd);
    }

    private boolean isForToday(Instant activationStart) {
        return getToday(clock).plusSeconds(activationStart.getEpochSecond()).isAfter(clock.instant());
    }

    public static Instant getToday(Clock clock) {
        return LocalDate.now(clock).atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    public static long getSecondsInDays(int days) {
        return days * 86400;
    }
}