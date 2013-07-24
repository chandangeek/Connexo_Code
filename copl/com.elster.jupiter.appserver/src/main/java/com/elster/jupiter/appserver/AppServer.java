package com.elster.jupiter.appserver;

import com.elster.jupiter.util.cron.CronExpression;

public interface AppServer {

    CronExpression getScheduleFrequency();

    String getName();

}
