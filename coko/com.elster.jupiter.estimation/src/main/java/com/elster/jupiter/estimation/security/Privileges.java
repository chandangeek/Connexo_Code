/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.security;

import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {
    //Resources
    RESOURCE_ESTIMATION_RULES("estimation.estimations", "Estimation"),
    RESOURCE_ESTIMATION_RULES_DESCRIPTION("estimation.estimations.description", "Manage estimation"),

    //Privileges
    ADMINISTRATE_ESTIMATION_CONFIGURATION(Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, "Administer configuration"),
    VIEW_ESTIMATION_CONFIGURATION(Constants.VIEW_ESTIMATION_CONFIGURATION, "View configuration"),
    UPDATE_ESTIMATION_CONFIGURATION(Constants.UPDATE_ESTIMATION_CONFIGURATION, "Update configuration"),
    UPDATE_SCHEDULE_ESTIMATION_TASK(Constants.UPDATE_SCHEDULE_ESTIMATION_TASK, "Update schedule"),
    RUN_ESTIMATION_TASK(Constants.RUN_ESTIMATION_TASK, "Run task"),
    FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE(Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE, "Fine tune device estimation"),
    FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE_CONFIGURATION(Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE_CONFIGURATION, "Fine tune device configuration estimation"),
    VIEW_ESTIMATION_TASK(Constants.VIEW_ESTIMATION_TASK, "View task"),
    ADMINISTRATE_ESTIMATION_TASK(Constants.ADMINISTRATE_ESTIMATION_TASK, "Administer task");

    private final String key;
    private final String description;

    Privileges(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return getDescription();
    }

    public String getDescription() {
        return description;
    }

    public static String[] keys() {
        return Arrays.stream(Privileges.values())
                .map(Privileges::getKey)
                .collect(Collectors.toList())
                .toArray(new String[Privileges.values().length]);
    }

    public interface Constants {
        String ADMINISTRATE_ESTIMATION_CONFIGURATION = "privilege.administrate.EstimationConfiguration";
        String VIEW_ESTIMATION_CONFIGURATION = "privilege.view.EstimationConfiguration";
        String UPDATE_ESTIMATION_CONFIGURATION = "privilege.update.EstimationConfiguration";
        String UPDATE_SCHEDULE_ESTIMATION_TASK = "privilege.update.ScheduleEstimationTask";
        String RUN_ESTIMATION_TASK = "privilege.run.ScheduleEstimationTask";
        String VIEW_ESTIMATION_TASK = "privilege.view.ScheduleEstimationTask";
        String ADMINISTRATE_ESTIMATION_TASK = "privilege.administrate.ScheduleEstimationTask";

        String FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE = "privilege.view.fineTuneEstimationConfiguration.onDevice";
        String FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE_CONFIGURATION = "privilege.view.fineTuneEstimationConfiguration.onDeviceConfiguration";
    }


}

