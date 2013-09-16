package com.elster.jupiter.appserver;

import com.elster.jupiter.util.cron.CronExpression;
import com.google.common.base.Optional;

import java.util.List;

public interface AppService {

    String ALL_SERVERS = "AllServers";

    Optional<AppServer> getAppServer();

    AppServer createAppServer(String name, CronExpression cronExpression);

    List<SubscriberExecutionSpec> getSubscriberExecutionSpecs();

    void stop();
}
