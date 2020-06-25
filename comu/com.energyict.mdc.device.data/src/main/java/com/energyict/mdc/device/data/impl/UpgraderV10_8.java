/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class UpgraderV10_8 implements Upgrader {
    private static final long PARTITIONSIZE = 86400L * 30L * 1000L;
    private final DataModel dataModel;
    private final Clock clock;
    private final InstallerV10_8Impl installerV10_8;
    private final TaskService taskService;

    @Inject
    UpgraderV10_8(DataModel dataModel, Clock clock, InstallerV10_8Impl installerV10_8, TaskService taskService) {
        this.dataModel = dataModel;
        this.clock = clock;
        this.installerV10_8 = installerV10_8;
        this.taskService = taskService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        boolean upgradeCRLneeded = dataModel.doesColumnExist(TableSpecs.DDC_CRL_REQUEST_TASK_PROPS.name(), "SECURITY_ACCESSOR");
        if (upgradeCRLneeded) {
            removeAllCRL();
        }
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 8));
        installerV10_8.install(dataModelUpgrader, Logger.getAnonymousLogger());
        if (upgradeCRLneeded) {
            updateCRLTable();
        }
        addAutoIncrementPartitions();
    }

    private void removeAllCRL() {
        List<RecurrentTask> relatedTasks = executeQuery(dataModel, "select TASK from " + TableSpecs.DDC_CRL_REQUEST_TASK_PROPS.name(), this::collectRecurrentTasks);
        execute(dataModel, "TRUNCATE TABLE " + TableSpecs.DDC_CRL_REQUEST_TASK_PROPS.name()); //need to delete props first
        relatedTasks.forEach(RecurrentTask::delete);
    }

    private List<RecurrentTask> collectRecurrentTasks(ResultSet resultSet) throws SQLException {
        List<RecurrentTask> recurrentTaskList = new ArrayList<>();
        while (resultSet.next()) {
            taskService.getRecurrentTask(resultSet.getLong("TASK")).ifPresent(recurrentTaskList::add);
        }
        return recurrentTaskList;
    }

    private void updateCRLTable() {
        execute(dataModel, "ALTER TABLE " + TableSpecs.DDC_CRL_REQUEST_TASK_PROPS.name() + " DROP COLUMN SECURITY_ACCESSOR");
    }

    private void addAutoIncrementPartitions() {
        //append partition for next month and enable auto increment partition interval
        if (dataModel.getSqlDialect().hasPartitioning()) {
            Arrays.asList("DDC_COMSESSION", "DDC_COMTASKEXECSESSION").forEach(tableName ->
                    execute(dataModel, "LOCK TABLE " + tableName + " PARTITION FOR (" + clock.instant().plusMillis(PARTITIONSIZE).toEpochMilli() + ") IN SHARE MODE",
                            "ALTER TABLE " + tableName + " SET INTERVAL (" + PARTITIONSIZE + ")")
            );
        }
    }
}
