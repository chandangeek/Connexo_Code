/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.security.Privileges;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.metering.GasDayOptions;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.time.DefaultRelativePeriodDefinition;
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
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.LAST_7_DAYS;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.PREVIOUS_MONTH;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.PREVIOUS_WEEK;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.THIS_MONTH;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.THIS_WEEK;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.THIS_YEAR;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.TODAY;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.YESTERDAY;

class Installer implements FullInstaller, PrivilegesProvider {

    private static final String DESTINATION_NAME = DataExportServiceImpl.DESTINATION_NAME;
    static final String SUBSCRIBER_NAME = DataExportServiceImpl.SUBSCRIBER_NAME;
    static final String RELATIVE_PERIOD_CATEGORY = "relativeperiod.category.dataExport";
    static final String RELATIVE_PERIOD_UPDATEWINDOW_CATEGORY = "relativeperiod.category.updateWindow";
    static final String RELATIVE_PERIOD_UPDATETIMEFRAME_CATEGORY = "relativeperiod.category.updateTimeframe";

    private final DataModel dataModel;
    private final MessageService messageService;
    private final TimeService timeService;
    private final UserService userService;

    @Inject
    Installer(DataModel dataModel, MessageService messageService, TimeService timeService, UserService userService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.timeService = timeService;
        this.userService = userService;
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
        DestinationSpec destinationSpec = queueTableSpec.createDestinationSpec(DESTINATION_NAME, 60);
        destinationSpec.save();
        destinationSpec.activate();
        destinationSpec.subscribe(TranslationKeys.SUBSCRIBER_NAME, DataExportService.COMPONENTNAME, Layer.DOMAIN);
    }

    private RelativePeriodCategory getCategory(String name) {
        return timeService.findRelativePeriodCategoryByName(name).orElseThrow(IllegalArgumentException::new);
    }

    private void createRelativePeriods() {
        this.requiredExportCategoryRelativePeriodNames()
                .forEach(name -> {
                    RelativePeriod relativePeriod =
                            timeService
                                    .findRelativePeriodByName(name)
                                    .orElseThrow(IllegalArgumentException::new);
                    relativePeriod.addRelativePeriodCategory(getCategory(RELATIVE_PERIOD_CATEGORY));
                });
        this.optionalExportCategoryRelativePeriodNames()
                .forEach(name ->
                        timeService
                                .findRelativePeriodByName(name)
                                .ifPresent(relativePeriod -> relativePeriod.addRelativePeriodCategory(getCategory(RELATIVE_PERIOD_CATEGORY))));

        this.requiredUpdateWindowCategoryRelativePeriodNames()
                .forEach(name -> {
                    RelativePeriod relativePeriod =
                            timeService
                                    .findRelativePeriodByName(name)
                                    .orElseThrow(IllegalArgumentException::new);
                    relativePeriod.addRelativePeriodCategory(getCategory(RELATIVE_PERIOD_UPDATEWINDOW_CATEGORY));
                });
        this.optionalUpdateWindowCategoryRelativePeriodNames()
                .forEach(name ->
                        timeService
                                .findRelativePeriodByName(name)
                                .ifPresent(relativePeriod -> relativePeriod.addRelativePeriodCategory(getCategory(RELATIVE_PERIOD_UPDATEWINDOW_CATEGORY))));

        this.requiredUpdateTimeFrameCategoryRelativePeriodNames()
                .forEach(name -> {
                    RelativePeriod relativePeriod =
                            timeService
                                    .findRelativePeriodByName(name)
                                    .orElseThrow(IllegalArgumentException::new);
                    relativePeriod.addRelativePeriodCategory(getCategory(RELATIVE_PERIOD_UPDATETIMEFRAME_CATEGORY));
                });
        this.optionalUpdateTimeFrameCategoryRelativePeriodNames()
                .forEach(name ->
                        timeService
                                .findRelativePeriodByName(name)
                                .ifPresent(relativePeriod -> relativePeriod.addRelativePeriodCategory(getCategory(RELATIVE_PERIOD_UPDATETIMEFRAME_CATEGORY))));
    }

    private Stream<String> requiredExportCategoryRelativePeriodNames() {
        return Stream.of(LAST_7_DAYS, PREVIOUS_MONTH, PREVIOUS_WEEK, THIS_MONTH, THIS_WEEK, TODAY, YESTERDAY).map(DefaultRelativePeriodDefinition::getPeriodName);
    }

    private Stream<String> optionalExportCategoryRelativePeriodNames() {
        return Stream.of(
                        GasDayOptions.RelativePeriodTranslationKey.LAST_7_DAYS,
                        GasDayOptions.RelativePeriodTranslationKey.PREVIOUS_MONTH,
                        GasDayOptions.RelativePeriodTranslationKey.PREVIOUS_WEEK,
                        GasDayOptions.RelativePeriodTranslationKey.THIS_MONTH,
                        GasDayOptions.RelativePeriodTranslationKey.THIS_WEEK,
                        GasDayOptions.RelativePeriodTranslationKey.TODAY,
                        GasDayOptions.RelativePeriodTranslationKey.YESTERDAY).map(GasDayOptions.RelativePeriodTranslationKey::getDefaultFormat);
    }

    private Stream<String> requiredUpdateWindowCategoryRelativePeriodNames() {
        return Stream.of(LAST_7_DAYS, PREVIOUS_MONTH, PREVIOUS_WEEK, YESTERDAY).map(DefaultRelativePeriodDefinition::getPeriodName);
    }

    private Stream<String> optionalUpdateWindowCategoryRelativePeriodNames() {
        return Stream.of(
                        GasDayOptions.RelativePeriodTranslationKey.LAST_7_DAYS,
                        GasDayOptions.RelativePeriodTranslationKey.PREVIOUS_MONTH,
                        GasDayOptions.RelativePeriodTranslationKey.PREVIOUS_WEEK,
                        GasDayOptions.RelativePeriodTranslationKey.YESTERDAY).map(GasDayOptions.RelativePeriodTranslationKey::getDefaultFormat);
    }

    private Stream<String> requiredUpdateTimeFrameCategoryRelativePeriodNames() {
        return Stream.of(THIS_MONTH, THIS_WEEK, THIS_YEAR, TODAY).map(DefaultRelativePeriodDefinition::getPeriodName);
    }

    private Stream<String> optionalUpdateTimeFrameCategoryRelativePeriodNames() {
        return Stream.of(
                        GasDayOptions.RelativePeriodTranslationKey.THIS_MONTH,
                        GasDayOptions.RelativePeriodTranslationKey.THIS_WEEK,
                        GasDayOptions.RelativePeriodTranslationKey.THIS_YEAR,
                        GasDayOptions.RelativePeriodTranslationKey.TODAY).map(GasDayOptions.RelativePeriodTranslationKey::getDefaultFormat);
    }

}