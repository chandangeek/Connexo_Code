package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.core.ScheduledJob;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareService;

import java.time.Instant;
import java.util.Calendar;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Models a {@link DeviceCommand} that reschedules a {@link ScheduledJob}
 * because the current timestamp is not within the {@link com.energyict.mdc.common.ComWindow}
 * of the related {@link com.energyict.mdc.device.data.tasks.ConnectionTask}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-23 (15:32)
 */
public class RescheduleToNextComWindow extends RescheduleExecutionDeviceCommand {

    private final static String DESCRIPTION_TITLE = "Reschedule to next communication window";
    private final FirmwareService firmwareService;

    public RescheduleToNextComWindow(JobExecution scheduledJob, FirmwareService firmwareService) {
        super(scheduledJob);
        this.firmwareService = firmwareService;
    }

    @Override
    protected void doExecute(ComServerDAO comServerDAO, JobExecution scheduledJob) {
        Instant startingPoint = getClock().instant();
        Optional<ComTaskExecution> firmwareComTaskExecution = scheduledJob.getComTaskExecutions().stream().filter(ComTaskExecution::isFirmware).findAny();
        if (firmwareComTaskExecution.isPresent()) {
            Optional<FirmwareCampaign> firmwareCampaign = firmwareService.getFirmwareCampaign(firmwareComTaskExecution.get());
            if (firmwareCampaign.isPresent()) {
                startingPoint = getComWindowAppliedStartDate(firmwareCampaign.get(), firmwareComTaskExecution.get().getNextExecutionTimestamp());
            }
        }
        scheduledJob.doRescheduleToNextComWindow(startingPoint);
    }

    private Instant getComWindowAppliedStartDate(FirmwareCampaign firmwareCampaign, Instant startDate) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(getClock().getZone()));
        calendar.setTimeInMillis(startDate.toEpochMilli());
        ComWindow comWindow = firmwareCampaign.getComWindow();
        if (comWindow.includes(calendar)) {
            return startDate;
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