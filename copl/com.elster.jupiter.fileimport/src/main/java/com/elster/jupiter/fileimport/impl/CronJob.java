package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.util.cron.CronExpression;

/**
 * Decorates a Runnable to have a CronExpression as a schedule.
 */
public interface CronJob extends Runnable {

    CronExpression getSchedule();

}
