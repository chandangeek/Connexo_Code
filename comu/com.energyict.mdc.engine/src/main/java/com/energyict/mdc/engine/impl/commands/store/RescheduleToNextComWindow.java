/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.interval.PartialTime;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.core.ScheduledJob;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaign;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Optional;

/**
 * Models a {@link DeviceCommand} that reschedules a {@link ScheduledJob}
 * because the current timestamp is not within the {@link com.energyict.mdc.common.ComWindow}
 * of the related {@link ConnectionTask}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-23 (15:32)
 */
public class RescheduleToNextComWindow extends RescheduleExecutionDeviceCommand {
    private final static String DESCRIPTION_TITLE = "Reschedule to next communication window";
    private final FirmwareService firmwareService;
    private final TimeOfUseCampaignService timeOfUseCampaignService;

    public RescheduleToNextComWindow(JobExecution scheduledJob, ServiceProvider serviceProvider, FirmwareService firmwareService, TimeOfUseCampaignService timeOfUseCampaignService) {
        super(scheduledJob, serviceProvider);
        this.firmwareService = firmwareService;
        this.timeOfUseCampaignService = timeOfUseCampaignService;
    }

    @Override
    protected void doExecute(ComServerDAO comServerDAO, JobExecution scheduledJob) {
        Instant startingPoint = getNow();
        Optional<Instant> instant = scheduledJob.getComTaskExecutions()
                .stream()
                .map(comTaskExecution -> {
                    Optional<FirmwareCampaign> firmwareCampaignOptional = firmwareService.getFirmwareCampaignService().getCampaignOn(comTaskExecution)
                            .filter(c -> c.getFirmwareUploadComTaskId() == comTaskExecution.getComTask().getId());
                    return firmwareCampaignOptional.map(firmwareCampaign -> getComWindowAppliedStartDate(firmwareCampaign.getComWindow(), comTaskExecution.getNextExecutionTimestamp()));
                })
                .flatMap(Functions.asStream())
                .findFirst();
        if (instant.isPresent()) {
            startingPoint = instant.get();
        }
        instant = scheduledJob.getComTaskExecutions()
                .stream()
                .map(comTaskExecution -> {
                    Optional<TimeOfUseCampaign> timeOfUseCampaignOptional = timeOfUseCampaignService.getCampaignOn(comTaskExecution)
                            .filter(c -> c.getCalendarUploadComTaskId() == comTaskExecution.getComTask().getId());
                    return timeOfUseCampaignOptional.map(timeOfUseCampaign -> getComWindowAppliedStartDate(timeOfUseCampaign.getComWindow(), comTaskExecution.getNextExecutionTimestamp()));
                })
                .flatMap(Functions.asStream())
                .findFirst();
        if (instant.isPresent()) {
            startingPoint = instant.get();
        }
        scheduledJob.doRescheduleToNextComWindow(startingPoint);
    }

    private Instant getComWindowAppliedStartDate(ComWindow comWindow, Instant startDate) {
        int SECONDS_IN_DAY = 86400;
        Calendar calendar = getUtcCalendar();
        calendar.setTimeInMillis(startDate.toEpochMilli());
        if (comWindow.includes(calendar)) {
            Calendar calendar2 = getUtcCalendar();
            PartialTime.fromHours(((GregorianCalendar) calendar).toZonedDateTime().getHour())
                    .plus(PartialTime.fromMinutes(((GregorianCalendar) calendar).toZonedDateTime().getMinute())).copyTo(calendar2);
            return getUtcCalendar().after(calendar2) ? calendar2.toInstant().plusSeconds(SECONDS_IN_DAY) : calendar2.toInstant();
        } else if (comWindow.after(calendar)) {
            comWindow.getStart().copyTo(calendar);
            return calendar.toInstant();
        } else {
            /* Timestamp must be after ComWindow,
             * advance one day and set time to start of the ComWindow. */
            calendar.add(Calendar.DATE, 1);
            comWindow.getStart().copyTo(calendar);
            return calendar.toInstant();
        }
    }

    public Calendar getUtcCalendar() {
        return GregorianCalendar.from(ZonedDateTime.ofInstant(getNow(), ZoneId.of("UTC"))); // obtain current calendar in UTC using getClock()
    }

    public Instant getNow() {
        return getClock().instant();
    }

    @Override
    public String getDescriptionTitle() {
        return DESCRIPTION_TITLE;
    }

}
