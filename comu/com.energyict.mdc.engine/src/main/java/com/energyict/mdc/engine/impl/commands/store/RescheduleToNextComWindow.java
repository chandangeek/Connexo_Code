/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.interval.PartialTime;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.core.ScheduledJob;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaign;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;

import java.time.Instant;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.TimeZone;

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

    public RescheduleToNextComWindow(JobExecution scheduledJob, FirmwareService firmwareService, TimeOfUseCampaignService timeOfUseCampaignService) {
        super(scheduledJob);
        this.firmwareService = firmwareService;
        this.timeOfUseCampaignService = timeOfUseCampaignService;
    }

    @Override
    protected void doExecute(ComServerDAO comServerDAO, JobExecution scheduledJob) {
        Instant startingPoint = getClock().instant();
        Optional<ComTaskExecution> firmwareComTaskExecution = scheduledJob.getComTaskExecutions().stream().filter(ComTaskExecution::isFirmware)
                .filter(comTaskExecution -> firmwareService.getFirmwareCampaignService().getCampaignOn(comTaskExecution).isPresent())
                .filter(comTaskExecution -> comTaskExecution.getComTask().getId() == firmwareService.getFirmwareCampaignService().getCampaignOn(comTaskExecution).get().getFirmwareUploadComTaskId())
                .findFirst();
        if (firmwareComTaskExecution.isPresent()) {
            FirmwareCampaign firmwareCampaign = firmwareService.getFirmwareCampaignService().getCampaignOn(firmwareComTaskExecution.get()).get();
            startingPoint = getComWindowAppliedStartDate(firmwareCampaign, firmwareComTaskExecution.get().getNextExecutionTimestamp());
        }
        Optional<ComTaskExecution> touComTaskExecution = scheduledJob.getComTaskExecutions().stream()
                .filter(comTaskExecution -> timeOfUseCampaignService.getCampaignOn(comTaskExecution).isPresent())
                .filter(comTaskExecution -> comTaskExecution.getComTask().getId() == timeOfUseCampaignService.getCampaignOn(comTaskExecution).get().getCalendarUploadComTaskId())
                .findFirst();
        if (touComTaskExecution.isPresent()) {
            TimeOfUseCampaign timeOfUseCampaign = timeOfUseCampaignService.getCampaignOn(touComTaskExecution.get()).get();
            startingPoint = getComWindowAppliedStartDate(timeOfUseCampaign, touComTaskExecution.get().getNextExecutionTimestamp());
        }
        scheduledJob.doRescheduleToNextComWindow(startingPoint);
    }

    private Instant getComWindowAppliedStartDate(Object campaign, Instant startDate) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(startDate.toEpochMilli());
        ComWindow comWindow = campaign instanceof FirmwareCampaign ? ((FirmwareCampaign) campaign).getComWindow() : ((TimeOfUseCampaign) campaign).getComWindow();
        if (comWindow.includes(calendar)) {
            Calendar calendar2 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            PartialTime.fromHours(((GregorianCalendar) calendar).toZonedDateTime().getHour())
                    .plus(PartialTime.fromMinutes(((GregorianCalendar) calendar).toZonedDateTime().getMinute())).copyTo(calendar2);
            return Calendar.getInstance(TimeZone.getTimeZone("UTC")).after(calendar2) ? calendar2.toInstant().plusSeconds(86400) : calendar2.toInstant();
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

    @Override
    public String getDescriptionTitle() {
        return DESCRIPTION_TITLE;
    }

}