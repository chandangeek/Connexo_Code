package com.elster.jupiter.appserver;

import com.elster.jupiter.util.cron.CronExpression;
import java.util.Optional;

import java.util.List;

public interface AppService {

    String ALL_SERVERS = "AllServers";
    String COMPONENT_NAME = "APS";

    Optional<AppServer> getAppServer();

    AppServer createAppServer(String name, CronExpression cronExpression);

    List<SubscriberExecutionSpec> getSubscriberExecutionSpecs();

    List<AppServer> findAppServers();

    void stop();
}
