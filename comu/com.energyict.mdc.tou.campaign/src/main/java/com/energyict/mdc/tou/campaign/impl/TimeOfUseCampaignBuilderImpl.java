/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.common.device.config.ConnectionStrategy;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaign;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignBuilder;
import com.energyict.mdc.tou.campaign.impl.servicecall.TimeOfUseCampaignDomainExtension;
import com.energyict.mdc.tou.campaign.impl.servicecall.TimeOfUseCampaignServiceImpl;

import java.time.Instant;
import java.util.Optional;

public class TimeOfUseCampaignBuilderImpl implements TimeOfUseCampaignBuilder {

    private String name;
    private DeviceType deviceType;
    private String deviceGroup;
    private Instant activationStart;
    private Instant activationEnd;
    private Calendar calendar;
    private String activationOption;
    private Instant activationDate;
    private String updateType;
    private Long validationTimeout;
    private boolean withUniqueCalendarName;
    private Long calendarUploadComTaskId;
    private Long validationComTaskId;
    private ConnectionStrategy calendarUploadConnectionStrategy;
    private ConnectionStrategy validationConnectionStrategy;

    private final TimeOfUseCampaignServiceImpl timeOfUseCampaignService;
    private final DataModel dataModel;

    public TimeOfUseCampaignBuilderImpl(TimeOfUseCampaignServiceImpl timeOfUseCampaignService, DataModel dataModel) {
        this.timeOfUseCampaignService = timeOfUseCampaignService;
        this.dataModel = dataModel;
    }

    @Override
    public TimeOfUseCampaignBuilder withUploadTimeBoundaries(Instant activationStart, Instant activationEnd) {
        this.activationStart = activationStart;
        this.activationEnd = activationEnd;
        return this;
    }

    @Override
    public TimeOfUseCampaignBuilder withActivationDate(Instant activationDate) {
        this.activationDate = activationDate;
        return this;
    }

    @Override
    public TimeOfUseCampaignBuilder withActivationOption(String activationOption) {
        this.activationOption = activationOption;
        return this;
    }

    @Override
    public TimeOfUseCampaignBuilder withCalendarUploadComTaskId(Long calendarUploadComTaskId) {
        this.calendarUploadComTaskId = calendarUploadComTaskId;
        return this;
    }

    @Override
    public TimeOfUseCampaignBuilder withValidationComTaskId(Long validationComTaskId) {
        this.validationComTaskId = validationComTaskId;
        return this;
    }

    @Override
    public TimeOfUseCampaignBuilder withCalendarUploadConnectionStrategy(String calendarUploadConnectionStrategy) {
        if (calendarUploadConnectionStrategy == null) {
            this.calendarUploadConnectionStrategy = null;
            return this;
        }
        this.calendarUploadConnectionStrategy = ConnectionStrategy.valueOf(calendarUploadConnectionStrategy.toUpperCase().replace(' ', '_'));
        return this;
    }

    @Override
    public TimeOfUseCampaignBuilder withValidationConnectionStrategy(String validationConnectionStrategy) {
        if (validationConnectionStrategy == null) {
            this.validationConnectionStrategy = null;
            return this;
        }
        this.validationConnectionStrategy = ConnectionStrategy.valueOf(validationConnectionStrategy.toUpperCase().replace(' ', '_'));
        return this;
    }

    @Override
    public TimeOfUseCampaignBuilder withDeviceGroup(String deviceGroup) {
        this.deviceGroup = deviceGroup;
        return this;
    }

    @Override
    public TimeOfUseCampaignBuilder withUpdateType(String updateType) {
        this.updateType = updateType;
        return this;
    }

    @Override
    public TimeOfUseCampaignBuilder withValidationTimeout(long validationTimeout) {
        this.validationTimeout = validationTimeout;
        return this;
    }

    @Override
    public TimeOfUseCampaignBuilder withUniqueCalendarName(boolean withCalendarNameValidation) {
        this.withUniqueCalendarName = withCalendarNameValidation;
        return this;
    }

    public TimeOfUseCampaignBuilderImpl withType(DeviceType deviceType) {
        this.deviceType = deviceType;
        return this;
    }

    public TimeOfUseCampaignBuilderImpl withCalendar(Calendar calendar) {
        this.calendar = calendar;
        return this;
    }

    public TimeOfUseCampaignBuilderImpl withName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public TimeOfUseCampaign create() {
        TimeOfUseCampaignDomainExtension timeOfUseCampaign = dataModel.getInstance(TimeOfUseCampaignDomainExtension.class);
        timeOfUseCampaign.setName(name);
        timeOfUseCampaign.setDeviceType(deviceType);
        timeOfUseCampaign.setDeviceGroup(deviceGroup);
        timeOfUseCampaign.setUploadPeriodStart(activationStart);
        timeOfUseCampaign.setUploadPeriodEnd(activationEnd);
        timeOfUseCampaign.setCalendar(calendar);
        timeOfUseCampaign.setUpdateType(updateType);
        timeOfUseCampaign.setActivationOption(activationOption);
        timeOfUseCampaign.setWithUniqueCalendarName(withUniqueCalendarName);
        timeOfUseCampaign.setCalendarUploadComTaskId(calendarUploadComTaskId);
        timeOfUseCampaign.setValidationComTaskId(validationComTaskId);
        timeOfUseCampaign.setCalendarUploadConnectionStrategy(calendarUploadConnectionStrategy);
        timeOfUseCampaign.setValidationConnectionStrategy(validationConnectionStrategy);
        Optional.ofNullable(activationDate).ifPresent(timeOfUseCampaign::setActivationDate);
        Optional.ofNullable(validationTimeout).ifPresent(timeOfUseCampaign::setValidationTimeout);
        ServiceCall serviceCall = timeOfUseCampaignService.createServiceCallAndTransition(timeOfUseCampaign);
        return timeOfUseCampaignService.getCampaign(serviceCall.getId()).orElseThrow(() -> new IllegalStateException("Just created campaign not found."));
    }
}
