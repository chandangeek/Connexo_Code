package com.elster.jupiter.appserver;

import com.elster.jupiter.util.cron.CronExpression;

import java.util.List;

public interface AppService {

    AppServer getAppServer();

    AppServer createAppServer(String name, CronExpression cronExpression);

    List<SubscriberExecutionSpec> getSubscriberExecutionSpecs();

    void stop();
}
