package com.elster.jupiter.appserver;

import com.elster.jupiter.appserver.impl.OrmClient;
import com.elster.jupiter.util.cron.CronExpression;

import java.util.List;

public interface AppService {

    OrmClient getOrmClient();

    AppServer getAppServer();

    AppServer createAppServer(String name, CronExpression cronExpression);

    List<SubscriberExecutionSpec> getSubscriberExecutionSpecs();

    void stop();
}
