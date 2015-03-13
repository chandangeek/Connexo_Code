package com.elster.jupiter.estimation.security;

public interface Privileges {

    String ADMINISTRATE_ESTIMATION_CONFIGURATION = "privilege.administrate.EstimationConfiguration";
    String VIEW_ESTIMATION_CONFIGURATION = "privilege.view.EstimationConfiguration";
    String UPDATE_ESTIMATION_CONFIGURATION = "privilege.update.EstimationConfiguration";
    String UPDATE_SCHEDULE_ESTIMATION_TASK = "privilege.update.ScheduleEstimationTask";
    String RUN_ESTIMATION_TASK = "privilege.run.ScheduleEstimationTask";

    String FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE = "privilege.view.fineTuneEstimationConfiguration.onDevice";
    String FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE_CONFIGURATION = "privilege.view.fineTuneEstimationConfiguration.onDeviceConfiguration";

}

