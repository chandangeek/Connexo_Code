package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.util.cron.CronExpression;

public interface AppServerCreator {

    AppServer createAppServer(final String name, final CronExpression cronExpression);

}
