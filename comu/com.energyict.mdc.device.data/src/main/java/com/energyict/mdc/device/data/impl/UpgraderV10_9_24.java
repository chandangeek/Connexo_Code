package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

public class UpgraderV10_9_24 implements Upgrader {
    private final DataModel dataModel;

    @Inject
    UpgraderV10_9_24(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        execute(dataModel, getDropJobStatement_con());
        execute(dataModel, getDropJobStatement_com());
        execute(dataModel, getRefreshConTaskDashboardJobStatement());
        execute(dataModel, getRefreshComTaskDashboardJobStatement());
    }

    private String getDropJobStatement_con() {
        return dataModel.getDropJobStatement("REF_CONTASK_DASHBOARD");
    }
    private String getDropJobStatement_com() {
        return dataModel.getDropJobStatement("REF_COMTASK_DASHBOARD");
    }


    private String getRefreshConTaskDashboardJobStatement() {
        return dataModel.getRefreshJobStatement("REF_CONTASK_DASHBOARD", "CONNECTION_TASK_STATUS();", 1);
    }

    private String getRefreshComTaskDashboardJobStatement() {
        return dataModel.getRefreshJobStatement("REF_COMTASK_DASHBOARD", "COMMUNICATION_TASK_STATUS();", 1);
    }
}