package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.util.cron.CronExpression;

public interface CronJob extends Runnable {

    CronExpression getSchedule();

}
