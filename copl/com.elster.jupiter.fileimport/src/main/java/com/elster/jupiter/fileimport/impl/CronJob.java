package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.time.ScheduleExpression;

/**
 * Decorates a Runnable to have a CronExpression as a schedule.
 */
interface CronJob extends Runnable {

    Long getId();
    ScheduleExpression getSchedule();

}
