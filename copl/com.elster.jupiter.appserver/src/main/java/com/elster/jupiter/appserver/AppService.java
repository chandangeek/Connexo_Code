/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.util.cron.CronExpression;

import aQute.bnd.annotation.ProviderType;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ProviderType
public interface AppService {

    /**
     * The name of the property that provides the name
     * of the server on which the AppServer is running.
     * When not set, the host name of the physical machine is used.
     */
    public static final String SERVER_NAME_PROPERTY_NAME = "com.elster.jupiter.server.name";

    String ALL_SERVERS = "AllServers";
    String COMPONENT_NAME = "APS";

    /**
     * Returns the AppServer currently configured for this machine. Keep in mind that the returned AppServer is not necessarily active.
     *
     * @return AppServer, empty is non is configured on the current machine.
     */
    Optional<AppServer> getAppServer();

    AppServer createAppServer(String name, CronExpression cronExpression);

    List<SubscriberExecutionSpec> getSubscriberExecutionSpecs();

    List<SubscriberExecutionSpec> getSubscriberExecutionSpecsFor(SubscriberSpec subscriberSpec);

    List<AppServer> findAppServers();

    Optional<AppServer> findAppServer(String name);

    Optional<AppServer> findAndLockAppServerByNameAndVersion(String name, long version);

    Query<AppServer> getAppServerQuery();

    void stop();

    Map<AppServer, Optional<Path>> getAllImportDirectories();

    List<AppServer> getImportScheduleAppServers(Long importScheduleId);
}
