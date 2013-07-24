package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.util.cron.CronExpression;

import java.util.List;

public interface AppService {

    OrmClient getOrmClient();

    AppServer getAppServer();

    AppServer createAppServer(String name, CronExpression cronExpression);

    List<SubscriberExecutionSpec> getSubscriberExecutionSpecs();
}
