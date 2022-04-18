/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.common.ComWindow;
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
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private final Logger debugLogger = Logger.getLogger(this.getClass().getName());

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

    /**
     * nextExecutionTimeStamp = start of ComWindow, but can be in the past!
     *
     * @param comWindow
     * @param nextExecutionTimeStamp
     * @return
     */
    public Instant getComWindowAppliedStartDate(ComWindow comWindow, Instant nextExecutionTimeStamp) {
        log("[FWC] getComWindowAppliedStartDate calculated from from " + nextExecutionTimeStamp.toString() + " to fit in comWindow: " + comWindow.toString());

        if (nextExecutionTimeStamp.isBefore(getNow())
                || !comWindow.includes(nextExecutionTimeStamp)) {
            Calendar startOfComWindow = getUtcCalendar();
            comWindow.getStart().copyTo(startOfComWindow);
            nextExecutionTimeStamp = startOfComWindow.toInstant();
            log("[FWC] getComWindowAppliedStartDate - adjusting to start of comWindow: " + nextExecutionTimeStamp.toString());
        }

        if (nextExecutionTimeStamp.isBefore(getNow())) {
            nextExecutionTimeStamp = nextExecutionTimeStamp.plus(1, ChronoUnit.DAYS);
            log("[FWC] getComWindowAppliedStartDate - still in the past adding one day: " + nextExecutionTimeStamp.toString());
        }

        if (nextExecutionTimeStamp.isBefore(getNow())) {
            log("[FWC] getComWindowAppliedStartDate - give up, still in the past: " + nextExecutionTimeStamp);
        } else {
            if (comWindow.includes(nextExecutionTimeStamp)) {
                log("[FWC] getComWindowAppliedStartDate - rescheduling to: " + nextExecutionTimeStamp);
                return nextExecutionTimeStamp;
            }
        }

        // fallback
        nextExecutionTimeStamp = nextExecutionTimeStamp.plus(1, ChronoUnit.DAYS);
        return nextExecutionTimeStamp;
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


    protected void log(String text) {
        if (debugLogger.isLoggable(Level.INFO)) {
            debugLogger.info("[ScheduledJob][RescheduleToNextComWindow]" + text);
        }
    }

}
