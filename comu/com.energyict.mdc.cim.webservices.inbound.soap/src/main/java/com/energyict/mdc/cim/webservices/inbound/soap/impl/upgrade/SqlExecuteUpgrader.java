
/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl.upgrade;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.upgrade.Upgrader;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.InboundSoapEndpointsActivator;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.TranslationKeys;
import com.energyict.mdc.cim.webservices.inbound.soap.task.FutureComTaskExecutionHandlerFactory;

import org.osgi.framework.BundleContext;

import java.sql.Statement;
import java.util.List;

/**
 * This class executes a list of SQL commands as upgrade
 *
 */
abstract class SqlExecuteUpgrader implements Upgrader {

    private static final String RECURENT_TASK_FREQUENCY = "com.energyict.mdc.cim.webservices.inbound.soap.recurenttaskfrequency";
    private static final String RECURENT_TASK_NAME = "FututeComTaskExecTask";
    private static final String RECURENT_TASK_SCHEDULE = "0 0/5 * 1/1 * ? *";
    private static final int RECURENT_TASK_RETRY_DELAY = 60;

    private final BundleContext bundleContext;
    private final MessageService messageService;
    private final TaskService taskService;

    private final OrmService ormService;

    SqlExecuteUpgrader(OrmService ormService, BundleContext bundleContext, MessageService messageService, TaskService taskService) {
        this.ormService = ormService;
        this.bundleContext = bundleContext;
        this.messageService = messageService;
        this.taskService = taskService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        ormService.getDataModel(MeteringService.COMPONENTNAME).get()
                .useConnectionRequiringTransaction(connection -> {
                    try (Statement statement = connection.createStatement()) {
                        getSQLStatementsToExecute().forEach(sqlCommand -> execute(statement, sqlCommand));
                    }
                });
        createFutureComTasksExecutionTask();
    }

    protected abstract List<String> getSQLStatementsToExecute();

    protected String ignoreColumnExistsOrTableDoesNotExist(String sql) {
        StringBuilder builder = new StringBuilder("declare column_exists exception;");
        builder.append("table_does_not_exist exception;");
        builder.append("pragma exception_init (column_exists , -01430);");
        builder.append("pragma exception_init (table_does_not_exist , -00942);");
        builder.append("begin execute immediate '");
        builder.append(sql);
        builder.append("';exception when column_exists then null;");
        builder.append("when table_does_not_exist then null; end;");
        return builder.toString();
    }

    private void createFutureComTasksExecutionTask() {
        String property = bundleContext.getProperty(RECURENT_TASK_FREQUENCY);
        createActionTask(FutureComTaskExecutionHandlerFactory.FUTURE_COM_TASK_EXECUTION_DESTINATION,
                RECURENT_TASK_RETRY_DELAY,
                TranslationKeys.FUTURE_COM_TASK_EXECUTION_NAME,
                RECURENT_TASK_NAME,
                property == null ? RECURENT_TASK_SCHEDULE : "0 0/" + property + " * 1/1 * ? *");
    }

    private void createActionTask(String destinationSpecName, int destinationSpecRetryDelay, TranslationKey subscriberSpecName, String taskName, String taskSchedule) {
        DestinationSpec destination = messageService.getQueueTableSpec("MSG_RAWTOPICTABLE")
                .get()
                .createDestinationSpec(destinationSpecName, destinationSpecRetryDelay);
        destination.activate();
        destination.subscribe(subscriberSpecName, InboundSoapEndpointsActivator.COMPONENT_NAME, Layer.DOMAIN);

        taskService.newBuilder()
                .setApplication("MultiSense")
                .setName(taskName)
                .setScheduleExpressionString(taskSchedule)
                .setDestination(destination)
                .setPayLoad("payload")
                .scheduleImmediately(true)
                .build();
    }
}
