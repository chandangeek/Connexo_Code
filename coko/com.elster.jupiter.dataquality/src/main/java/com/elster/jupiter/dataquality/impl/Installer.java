/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppServerCommand;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.Command;
import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.dataquality.impl.calc.DataQualityKpiCalculatorHandlerFactory;
import com.elster.jupiter.dataquality.security.Privileges;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.SqlDialect;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class Installer implements FullInstaller, PrivilegesProvider {

    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;

    private final DataModel dataModel;
    private final MessageService messageService;
    private final UserService userService;
    private final AppService appService;

    @Inject
    public Installer(DataModel dataModel, MessageService messageService, UserService userService, AppService appService) {
        super();
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.userService = userService;
        this.appService = appService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());

        doTry(
                "Create data quality queue",
                this::createMessageHandlers,
                logger
        );

        doTry(
                "Create Sql Function",
                this::createKpiAggregationFunction,
                logger
        );

        if (validationKpiInstalled()) {
            doTry(
                    "Migrate from validation kpi",
                    this::migrateFromValidationKpi,
                    logger
            );
        }

        userService.addModulePrivileges(this);
    }

    private boolean validationKpiInstalled() {
        return messageService.getDestinationSpec("ValKpiCalcTopic").isPresent();
    }

    private void createMessageHandlers() {
        QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        this.createMessageHandler(defaultQueueTableSpec, DataQualityKpiCalculatorHandlerFactory.TASK_DESTINATION, TranslationKeys.KPICALCULATOR_DISPLAYNAME);
    }

    private void createMessageHandler(QueueTableSpec defaultQueueTableSpec, String destinationName, TranslationKey subscriberName) {
        Optional<DestinationSpec> destinationSpecOptional = messageService.getDestinationSpec(destinationName);
        if (!destinationSpecOptional.isPresent()) {
            DestinationSpec queue = defaultQueueTableSpec.createDestinationSpec(destinationName, DEFAULT_RETRY_DELAY_IN_SECONDS);
            queue.activate();
            queue.subscribe(subscriberName, DataQualityKpiService.COMPONENTNAME, Layer.DOMAIN);
        } else {
            boolean notSubscribedYet = !destinationSpecOptional.get().getSubscribers().stream().anyMatch(spec -> spec.getName().equals(subscriberName.getKey()));
            if (notSubscribedYet) {
                destinationSpecOptional.get().activate();
                destinationSpecOptional.get().subscribe(subscriberName, DataQualityKpiService.COMPONENTNAME, Layer.DOMAIN);
            }
        }
    }

    private void createKpiAggregationFunction() {
        List<String> sql = new ArrayList<>();
        sql.add(utc2dateSqlFunction());

        if (!SqlDialect.H2.equals(dataModel.getSqlDialect())) {
            dataModel.useConnectionRequiringTransaction(connection -> {
                try (Statement statement = connection.createStatement()) {
                    sql.forEach(sqlCommand -> execute(statement, sqlCommand));
                }
            });
        }
    }

    private String utc2dateSqlFunction() {
        return "create or replace function utc2date(utcms number, tz varchar2)" +
                " return timestamp with time zone deterministic" +
                " is" +
                " begin" +
                "   return from_tz(cast(date'1970-1-1' + (utcms/86400000) as timestamp),'UTC') at time zone tz;" +
                " end;";
    }

    private void execute(Statement statement, String sql) {
        try {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    @Override
    public String getModuleName() {
        return DataQualityKpiService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(createDataQualityResource());
        return resources;
    }

    private ResourceDefinition createDataQualityResource() {
        return userService.createModuleResourceWithPrivileges(
                DataQualityKpiService.COMPONENTNAME,
                Privileges.RESOURCE_QUALITY.getKey(),
                Privileges.RESOURCE_QUALITY_DESCRIPTION.getKey(),
                Arrays.asList(
                        Privileges.Constants.ADMINISTER_DATA_QUALITY_KPI_CONFIGURATION,
                        Privileges.Constants.VIEW_DATA_QUALITY_KPI_CONFIGURATION,
                        Privileges.Constants.VIEW_DATA_QUALITY_RESULTS
                )
        );
    }

    private void migrateFromValidationKpi() {
        upgradeKpiTables();
        upgradeKpiMessageHandler();
    }

    private void upgradeKpiTables() {
        List<String> sql = new ArrayList<>();
        sql.add("INSERT INTO DQK_DATAQUALITYKPI(id, dataqualitykpitask, enddevicegroup, versioncount, createtime, modtime, username, discriminator, usagepointgroup, metrologypurpose)" +
                " SELECT DQK_DATAQUALITYKPIID.nextval, datavalidationkpi_task, enddevicegroup, versioncount, createtime, modtime, username, 'EDDQ', NULL, NULL FROM VAL_DATA_VALIDATION_KPI");
        sql.add("INSERT INTO DQK_DATAQUALITYKPIMEMBER (dataqualitykpi, childkpi)" +
                " SELECT dqk.id, dvkc.childkpi" +
                " FROM VAL_DATA_VALIDATION_KPI dvk" +
                " JOIN VAL_DATAVALIDATIONKPICHILDREN dvkc ON dvk.id = dvkc.datavalidationkpi" +
                " JOIN DQK_DATAQUALITYKPI dqk ON dvk.datavalidationkpi_task = dqk.dataqualitykpitask");
        sql.add("DROP TABLE VAL_DATAVALIDATIONKPICHILDREN");
        sql.add("DROP TABLE VAL_DATA_VALIDATION_KPI");
        sql.add("UPDATE TSK_RECURRENT_TASK tsk" +
                " SET destination = 'DataQualityKpiCalcTopic'," +
                "     payload = 'DEVICE_DATA_QUALITY_KPI-' || (SELECT dqk.id FROM DQK_DATAQUALITYKPI dqk WHERE tsk.id = dqk.dataqualitykpitask)," +
                "     name = (SELECT edg.name || ' - Device Data Quality KPI' FROM DQK_DATAQUALITYKPI dqk JOIN MTG_ED_GROUP edg ON dqk.ENDDEVICEGROUP = edg.id WHERE tsk.id = dqk.dataqualitykpitask)" +
                " WHERE id IN (SELECT dataqualitykpitask FROM DQK_DATAQUALITYKPI)");

        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                sql.forEach(sqlCommand -> execute(statement, sqlCommand));
            }
        });
    }

    private void upgradeKpiMessageHandler() {
        Optional<SubscriberSpec> validationKpiSubscriber = messageService.getSubscriberSpec("ValKpiCalcTopic", "ValKpiCalc");
        Optional<SubscriberSpec> dataQualityKpiSubscriber = messageService.getSubscriberSpec(
                DataQualityKpiCalculatorHandlerFactory.TASK_DESTINATION, DataQualityKpiCalculatorHandlerFactory.TASK_SUBSCRIBER);
        if (validationKpiSubscriber.isPresent() && dataQualityKpiSubscriber.isPresent()) {
            replaceSubscriber(validationKpiSubscriber.get(), dataQualityKpiSubscriber.get());
        }
        messageService.getDestinationSpec("ValKpiCalcTopic").ifPresent(DestinationSpec::delete);
    }

    private void replaceSubscriber(SubscriberSpec obsoleteSubscriber, SubscriberSpec newSubscriber) {
        appService.findAppServers().stream()
                .map(AppServer::getSubscriberExecutionSpecs)
                .flatMap(Collection::stream)
                .filter(spec -> spec.getSubscriberSpec().getName().equals(obsoleteSubscriber.getName()))
                .filter(spec -> spec.getSubscriberSpec().getDestination().getName().equals(obsoleteSubscriber.getDestination().getName()))
                .forEach(subscriberSpec -> {
                    AppServer appServer = subscriberSpec.getAppServer();
                    appServer.removeSubscriberExecutionSpec(subscriberSpec);
                    appServer.createSubscriberExecutionSpec(newSubscriber, subscriberSpec.getThreadCount());
                    appServer.sendCommand(new AppServerCommand(Command.CONFIG_CHANGED));
                });
    }
}