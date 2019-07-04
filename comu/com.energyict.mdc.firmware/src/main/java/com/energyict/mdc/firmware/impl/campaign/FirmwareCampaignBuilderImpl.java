/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl.campaign;


import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaignBuilder;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FirmwareCampaignBuilderImpl implements FirmwareCampaignBuilder {

    String name;
    DeviceType deviceType;
    String deviceGroup;
    Instant uploadStart;
    Instant uploadEnd;
    Instant activationDate;
    TimeDuration validationTimeout;
    ProtocolSupportedFirmwareOptions protocolSupportedFirmwareOptions;
    FirmwareType firmwareType;
    Map<PropertySpec, Object> properties;
    long calendarUploadConnectionStrategyId;
    long validationComTaskId;
    long validationConnectionStrategyId;

    private final FirmwareCampaignServiceImpl firmwareCampaignService;
    private final DataModel dataModel;

    public FirmwareCampaignBuilderImpl(FirmwareCampaignServiceImpl firmwareCampaignService, DataModel dataModel) {
        this.firmwareCampaignService = firmwareCampaignService;
        this.dataModel = dataModel;
        this.properties = new HashMap<>();
    }

    @Override
    public FirmwareCampaignBuilder withUploadTimeBoundaries(Instant uploadStart, Instant uploadEnd) {
        this.uploadStart = uploadStart;
        this.uploadEnd = uploadEnd;
        return this;
    }

    @Override
    public FirmwareCampaignBuilder withDeviceGroup(String deviceGroup) {
        this.deviceGroup = deviceGroup;
        return this;
    }

    @Override
    public FirmwareCampaignBuilder withValidationTimeout(TimeDuration validationTimeout) {
        this.validationTimeout = validationTimeout;
        return this;
    }

    @Override
    public FirmwareCampaignBuilderImpl withDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
        return this;
    }

    @Override
    public FirmwareCampaignBuilderImpl withFirmwareType(FirmwareType firmwareType) {
        this.firmwareType = firmwareType;
        return this;
    }

    public FirmwareCampaignBuilderImpl withName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public FirmwareCampaignBuilderImpl withManagementOption(ProtocolSupportedFirmwareOptions protocolSupportedFirmwareOptions) {
        this.protocolSupportedFirmwareOptions = protocolSupportedFirmwareOptions;
        return this;
    }

    @Override
    public FirmwareCampaignBuilderImpl withValidationComTaskId(long validationComTaskId){
        this.validationComTaskId = validationComTaskId;
        return this;
    }

    @Override
    public FirmwareCampaignBuilderImpl withCalendarUploadConnectionStrategyId( long calendarUploadConnectionStrategyId){
        this.calendarUploadConnectionStrategyId = calendarUploadConnectionStrategyId;
        return this;
    }

    @Override
    public FirmwareCampaignBuilderImpl withValidationConnectionStrategyId(long validationConnectionStrategyId){
        this.validationConnectionStrategyId = validationConnectionStrategyId;
        return this;
    }

    @Override
    public FirmwareCampaignBuilder addProperty(PropertySpec propertySpec, Object propertyValue) {
        this.properties.put(propertySpec, propertyValue);
        if (propertySpec.getName().equals("FirmwareDeviceMessage.upgrade.activationdate")) {
            this.activationDate = (((Date) propertyValue).toInstant());
        }
        return this;
    }

    @Override
    public FirmwareCampaign create() {
        FirmwareCampaignDomainExtension firmwareCampaign = dataModel.getInstance(FirmwareCampaignDomainExtension.class);
        firmwareCampaign.setName(name);
        firmwareCampaign.setDeviceType(deviceType);
        firmwareCampaign.setDeviceGroup(deviceGroup);
        firmwareCampaign.setFirmwareType(firmwareType);
        firmwareCampaign.setUploadPeriodStart(uploadStart);
        firmwareCampaign.setUploadPeriodEnd(uploadEnd);
        firmwareCampaign.setManagementOption(protocolSupportedFirmwareOptions);
        firmwareCampaign.setCalendarUploadConnectionStrategyId(calendarUploadConnectionStrategyId);
        firmwareCampaign.setValidationComTaskId(validationComTaskId);
        firmwareCampaign.setValidationConnectionStrategyId(validationConnectionStrategyId);
        Optional.ofNullable(activationDate).ifPresent(firmwareCampaign::setActivationDate);
        Optional.ofNullable(validationTimeout).ifPresent(firmwareCampaign::setValidationTimeout);
        ServiceCall serviceCall = firmwareCampaignService.createServiceCallAndTransition(firmwareCampaign);
        FirmwareCampaign firmwareCampaign2 = firmwareCampaignService.getFirmwareCampaignById(serviceCall.getId()).orElseThrow(() -> new IllegalStateException("Just created campaign not found."));
        firmwareCampaign2.addProperties(properties);
        return firmwareCampaign2;
    }
}
