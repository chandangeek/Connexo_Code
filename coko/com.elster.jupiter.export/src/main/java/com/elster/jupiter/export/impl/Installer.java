package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.security.Privileges;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.RelativePeriodCategory;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Logger;

import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.LAST_7_DAYS;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.PREVIOUS_MONTH;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.PREVIOUS_WEEK;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.THIS_MONTH;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.THIS_WEEK;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.THIS_YEAR;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.TODAY;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.YESTERDAY;

class Installer implements FullInstaller, PrivilegesProvider {

    public static final String DESTINATION_NAME = DataExportServiceImpl.DESTINATION_NAME;
    public static final String SUBSCRIBER_NAME = DataExportServiceImpl.SUBSCRIBER_NAME;
    public static final String RELATIVE_PERIOD_CATEGORY = "relativeperiod.category.dataExport";
    public static final String RELATIVE_PERIOD_UPDATEWINDOW_CATEGORY = "relativeperiod.category.updateWindow";
    public static final String RELATIVE_PERIOD_UPDATETIMEFRAME_CATEGORY = "relativeperiod.category.updateTimeframe";

    private final DataModel dataModel;
    private final MessageService messageService;
    private final TimeService timeService;
    private final UserService userService;

    private DestinationSpec destinationSpec;

    @Inject
    Installer(DataModel dataModel, MessageService messageService, TimeService timeService, UserService userService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.timeService = timeService;
        this.userService = userService;
    }

    public DestinationSpec getDestinationSpec() {
        return destinationSpec;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        doTry(
                "Create Data Export queue",
                this::createDestinationAndSubscriber,
                logger
        );
        doTry(
                "Create export Relative Period category",
                this::createRelativePeriodCategory,
                logger
        );
        doTry(
                "Create relative periods",
                this::createRelativePeriods,
                logger
        );
        userService.addModulePrivileges(this);
    }

    @Override
    public String getModuleName() {
        return DataExportService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(getModuleName(),
                Privileges.RESOURCE_DATA_EXPORT.getKey(), Privileges.RESOURCE_DATA_EXPORT_DESCRIPTION.getKey(),
                Arrays.asList(Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK,
                        Privileges.Constants.VIEW_DATA_EXPORT_TASK,
                        Privileges.Constants.UPDATE_DATA_EXPORT_TASK,
                        Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK,
                        Privileges.Constants.RUN_DATA_EXPORT_TASK)));
        return resources;
    }

    private void createRelativePeriodCategory() {
        timeService.createRelativePeriodCategory(RELATIVE_PERIOD_CATEGORY);
        timeService.createRelativePeriodCategory(RELATIVE_PERIOD_UPDATEWINDOW_CATEGORY);
        timeService.createRelativePeriodCategory(RELATIVE_PERIOD_UPDATETIMEFRAME_CATEGORY);
    }

    private void createDestinationAndSubscriber() {
        QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        destinationSpec = queueTableSpec.createDestinationSpec(DESTINATION_NAME, 60);
        destinationSpec.save();
        destinationSpec.activate();
        destinationSpec.subscribe(SUBSCRIBER_NAME).create();
    }

    private RelativePeriodCategory getCategory(String name) {
        return timeService.findRelativePeriodCategoryByName(name).orElseThrow(IllegalArgumentException::new);
    }

    private void createRelativePeriods() {
        EnumSet.of(LAST_7_DAYS, PREVIOUS_MONTH, PREVIOUS_WEEK, THIS_MONTH, THIS_WEEK, TODAY, YESTERDAY)
                .forEach(definition -> {
                    RelativePeriod relativePeriod = timeService.findRelativePeriodByName(definition.getPeriodName())
                            .orElseThrow(IllegalArgumentException::new);
                    relativePeriod.addRelativePeriodCategory(getCategory(RELATIVE_PERIOD_CATEGORY));
                });

        EnumSet.of(LAST_7_DAYS, PREVIOUS_MONTH, PREVIOUS_WEEK, YESTERDAY)
                .forEach(definition -> {
                    RelativePeriod relativePeriod = timeService.findRelativePeriodByName(definition.getPeriodName())
                            .orElseThrow(IllegalArgumentException::new);
                    relativePeriod.addRelativePeriodCategory(getCategory(RELATIVE_PERIOD_UPDATEWINDOW_CATEGORY));
                });

        EnumSet.of(THIS_MONTH, THIS_WEEK, THIS_YEAR, TODAY)
                .forEach(definition -> {
                    RelativePeriod relativePeriod = timeService.findRelativePeriodByName(definition.getPeriodName())
                            .orElseThrow(IllegalArgumentException::new);
                    relativePeriod.addRelativePeriodCategory(getCategory(RELATIVE_PERIOD_UPDATETIMEFRAME_CATEGORY));
                });
    }

}