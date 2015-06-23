package com.elster.jupiter.appserver;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.util.cron.CronExpression;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import java.util.List;

public interface AppService {

    /**
     * The name of the property that provides the name
     * of the server on which the AppServer is running.
     * When not set, the host name of the physical machine is used.
     */
    public static final String SERVER_NAME_PROPERTY_NAME = "com.elster.jupiter.server.name";

    String ALL_SERVERS = "AllServers";
    String COMPONENT_NAME = "APS";

    Optional<AppServer> getAppServer();

    AppServer createAppServer(String name, CronExpression cronExpression);

    List<SubscriberExecutionSpec> getSubscriberExecutionSpecs();

    List<AppServer> findAppServers();

    Optional<AppServer> findAppServer(String name);

    Query<AppServer> getAppServerQuery();

    void stop();

    Map<AppServer, Optional<Path>> getAllImportDirectories();

    List<AppServer> getImportScheduleAppServers(Long importScheduleId);
}
