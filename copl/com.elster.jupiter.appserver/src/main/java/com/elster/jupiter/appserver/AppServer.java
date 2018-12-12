/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver;

import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.util.cron.CronExpression;

import aQute.bnd.annotation.ProviderType;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface AppServer {

    CronExpression getScheduleFrequency();

    String getName();

	List<? extends SubscriberExecutionSpec> getSubscriberExecutionSpecs();

    List<? extends ImportScheduleOnAppServer> getImportSchedulesOnAppServer();

	SubscriberExecutionSpec createSubscriberExecutionSpec(SubscriberSpec subscriberSpec, int threadCount);

    ImportScheduleOnAppServer addImportScheduleOnAppServer(ImportSchedule importSchedule);

    void sendCommand(AppServerCommand command);

    void setRecurrentTaskActive(boolean recurrentTaskActive);

    boolean isRecurrentTaskActive();

    void removeSubscriberExecutionSpec(SubscriberExecutionSpec subscriberExecutionSpec);

    void removeImportScheduleOnAppServer(ImportScheduleOnAppServer importScheduleOnAppServer);

    boolean isActive();

    void activate();

    void deactivate();

    void setThreadCount(SubscriberExecutionSpec subscriberExecutionSpec, int threads);

    void setImportDirectory(Path path);
    void removeImportDirectory();
    Optional<Path> getImportDirectory();

    BatchUpdate forBatchUpdate();

    void delete();
    
    long getVersion();

    /**
     * Enable support for an mentioned EndPoint, using the mentioned configuration. Whenever the endpoint becomes
     * enabled, this appserver will publish it. An enabled endpoint will be published on all appservers it is supported
     * on. Support of an endpoint can be dropped with dropEndPointSupport
     *
     * @param endPointConfiguration The configuration to use to publish an endpoint
     */
    void supportEndPoint(EndPointConfiguration endPointConfiguration);

    /**
     * Stop supporting an EndPoint. If an EndPoint is currently published on the AppServer, it will be removed and from now on,
     * EndPoint will no longer be published for this EndPointConfiguration on the AppServer if the EndPointConfig is enabled.
     * If you wnt to start supporting an EndPoint again, use supportConfiguration.
     *
     * @param endPointConfiguration The configuration to disable
     */
    void dropEndPointSupport(EndPointConfiguration endPointConfiguration);

    /**
     * Get a list of EndPointConfigurations that will be used to publish endpoints once the config is enabled.
     * Once the config is enabled, this appserver will publish an endpoint (for inbound) or register a service (for outbound)
     * Upon disabling, the created object will be torn down again.
     * Only inbound endpoints can be configured on a per-appserver basis, outbound endpoints will always be supported
     *
     * @return List of supported end points
     */
    List<EndPointConfiguration> supportedEndPoints();

    interface BatchUpdate extends AutoCloseable {

        SubscriberExecutionSpec createActiveSubscriberExecutionSpec(SubscriberSpec subscriberSpec, int threadCount);

        SubscriberExecutionSpec createInactiveSubscriberExecutionSpec(SubscriberSpec subscriberSpec, int threadCount);

        void removeSubscriberExecutionSpec(SubscriberExecutionSpec subscriberExecutionSpec);

        ImportScheduleOnAppServer addImportScheduleOnAppServer(ImportSchedule importSchedule);

        void removeImportScheduleOnAppServer(ImportScheduleOnAppServer importScheduleOnAppServer);

        void setRecurrentTaskActive(boolean recurrentTaskActive);

        void setThreadCount(SubscriberExecutionSpec subscriberExecutionSpec, int threads);

        void activate(SubscriberExecutionSpec subscriberExecutionSpec);

        void deactivate(SubscriberExecutionSpec subscriberExecutionSpec);

        void activate();

        void deactivate();

        @Override
        void close();

        void delete();

    }
}
