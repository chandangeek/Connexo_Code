/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.rest.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaign;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignBuilder;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;

import static com.energyict.mdc.tou.campaign.rest.impl.RestUtil.getCampaignStatus;
import static com.energyict.mdc.tou.campaign.rest.impl.RestUtil.getDeviceStatus;

public class TimeOfUseCampaignInfoFactory {

    private final TimeOfUseCampaignService timeOfUseCampaignService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final CalendarService calendarService;
    private final ExceptionFactory exceptionFactory;
    private final Clock clock;
    private final Thesaurus thesaurus;

    @Inject
    public TimeOfUseCampaignInfoFactory(TimeOfUseCampaignService timeOfUseCampaignService, Clock clock, Thesaurus thesaurus,
                                        DeviceConfigurationService deviceConfigurationService, CalendarService calendarService,
                                        ExceptionFactory exceptionFactory) {
        this.timeOfUseCampaignService = timeOfUseCampaignService;
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.deviceConfigurationService = deviceConfigurationService;
        this.calendarService = calendarService;
        this.exceptionFactory = exceptionFactory;
    }

    public TimeOfUseCampaign build(TimeOfUseCampaignInfo timeOfUseCampaignInfo) {
        Range<Instant> timeFrame = retrieveRealUploadRange(timeOfUseCampaignInfo);
        DeviceType deviceType = deviceConfigurationService.findDeviceType(((Number) timeOfUseCampaignInfo.deviceType.id).longValue())
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICETYPE_WITH_ID_ISNT_FOUND, timeOfUseCampaignInfo.deviceType.id));
        Calendar calendar = calendarService.findCalendar(((Number) timeOfUseCampaignInfo.calendar.id).longValue())
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.CALENDAR_WITH_ID_ISNT_FOUND, timeOfUseCampaignInfo.calendar.id));
        TimeOfUseCampaignBuilder timeOfUseCampaignBuilder = timeOfUseCampaignService
                .newTouCampaignBuilder(timeOfUseCampaignInfo.name, deviceType, calendar)
                .withDeviceGroup(timeOfUseCampaignInfo.deviceGroup)
                .withActivationOption(timeOfUseCampaignInfo.activationOption)
                .withActivationDate(timeOfUseCampaignInfo.activationDate)
                .withUpdateType(timeOfUseCampaignInfo.updateType)
                .withValidationTimeout(timeOfUseCampaignInfo.validationTimeout)
                .withUploadTimeBoundaries(timeFrame.lowerEndpoint(), timeFrame.upperEndpoint());
        return timeOfUseCampaignBuilder.create();
    }

    public TimeOfUseCampaignInfo from(TimeOfUseCampaign campaign) {
        TimeOfUseCampaignInfo timeOfUseCampaignInfo = new TimeOfUseCampaignInfo();
        timeOfUseCampaignInfo.name = campaign.getName();
        timeOfUseCampaignInfo.deviceType = new IdWithNameInfo(campaign.getDeviceType().getId(), campaign.getDeviceType().getName());
        timeOfUseCampaignInfo.deviceGroup = campaign.getDeviceGroup();
        timeOfUseCampaignInfo.activationStart = campaign.getUploadPeriodStart();
        timeOfUseCampaignInfo.activationEnd = campaign.getUploadPeriodEnd();
        timeOfUseCampaignInfo.calendar = new IdWithNameInfo(campaign.getCalendar().getId(), campaign.getCalendar().getName());
        timeOfUseCampaignInfo.updateType = campaign.getUpdateType();
        timeOfUseCampaignInfo.activationOption = campaign.getActivationOption();
        timeOfUseCampaignInfo.activationDate = campaign.getActivationDate();
        timeOfUseCampaignInfo.validationTimeout = campaign.getValidationTimeout();
        timeOfUseCampaignInfo.id = campaign.getId();
        timeOfUseCampaignInfo.version = campaign.getVersion();
        timeOfUseCampaignInfo.withUniqueCalendarName = campaign.isWithUniqueCalendarName();
        return timeOfUseCampaignInfo;
    }

    public TimeOfUseCampaignInfo getOverviewCampaignInfo(TimeOfUseCampaign campaign) {
        TimeOfUseCampaignInfo info = from(campaign);
        ServiceCall campaignsServiceCall = campaign.getServiceCall();
        info.startedOn = campaignsServiceCall.getCreationTime();
        info.finishedOn = (campaignsServiceCall.getState().equals(DefaultState.CANCELLED)
                || campaignsServiceCall.getState().equals(DefaultState.SUCCESSFUL)) ? campaignsServiceCall.getLastModificationTime() : null;
        info.status = getCampaignStatus(campaignsServiceCall.getState(), thesaurus);
        info.devices = new ArrayList<>();
        info.devices.add(new DevicesStatusAndQuantity(getDeviceStatus(DefaultState.SUCCESSFUL, thesaurus), 0L));
        info.devices.add(new DevicesStatusAndQuantity(getDeviceStatus(DefaultState.FAILED, thesaurus), 0L));
        info.devices.add(new DevicesStatusAndQuantity(getDeviceStatus(DefaultState.REJECTED, thesaurus), 0L));
        info.devices.add(new DevicesStatusAndQuantity(getDeviceStatus(DefaultState.ONGOING, thesaurus), 0L));
        info.devices.add(new DevicesStatusAndQuantity(getDeviceStatus(DefaultState.PENDING, thesaurus), 0L));
        info.devices.add(new DevicesStatusAndQuantity(getDeviceStatus(DefaultState.CANCELLED, thesaurus), 0L));
        campaign.getNumbersOfChildrenWithStatuses().forEach((deviceStatus, quantity) ->
                info.devices.stream().filter(devicesStatusAndQuantity -> devicesStatusAndQuantity.status.equals(getDeviceStatus(deviceStatus, thesaurus)))
                        .findAny().ifPresent(devicesStatusAndQuantity -> devicesStatusAndQuantity.quantity = quantity));
        return info;
    }

    private boolean isForToday(Instant activationStart) {
        return getToday(clock).plusSeconds(activationStart.getEpochSecond()).isAfter(clock.instant());
    }

    public static Instant getToday(Clock clock) {
        return Instant.parse(clock.instant().toString().substring(0, 11) + "00:00:00Z");
    }

    public static long getSecondsInDays(int days) {
        return days * 86400;
    }

    public Range<Instant> retrieveRealUploadRange(TimeOfUseCampaignInfo timeOfUseCampaignInfo) {
        Instant activationStart = timeOfUseCampaignInfo.activationStart;
        Instant activationEnd = timeOfUseCampaignInfo.activationEnd;
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
}
