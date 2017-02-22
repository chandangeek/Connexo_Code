/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.impl;

import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StageSet;
import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class UpgraderV10_3 implements Upgrader {
    private final DataModel dataModel;
    private final FiniteStateMachineService finiteStateMachineService;

    @Inject
    public UpgraderV10_3(DataModel dataModel, FiniteStateMachineService finiteStateMachineService) {
        this.dataModel = dataModel;
        this.finiteStateMachineService = finiteStateMachineService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        List<String> sql = new ArrayList<>();
        StageSet stageSet = finiteStateMachineService.findStageSetByName(MeteringService.END_DEVICE_STAGE_SET_NAME).orElseThrow(getDeviceStageSetException());
        long preOperationalId = stageSet.getStageByName(EndDeviceStage.PRE_OPERATIONAL.name()).orElseThrow(getDeviceStageSetException()).getId();
        long operationalId = stageSet.getStageByName(EndDeviceStage.OPERATIONAL.name()).orElseThrow(getDeviceStageSetException()).getId();
        long postOperationalId = stageSet.getStageByName(EndDeviceStage.POST_OPERATIONAL.name()).orElseThrow(getDeviceStageSetException()).getId();

        sql.add("UPDATE FSM_FINITE_STATE_MACHINE SET STAGE_SET = " + stageSet.getId() +" WHERE ID IN (SELECT FSM FROM DLD_DEVICE_LIFE_CYCLE)");
        sql.add("UPDATE FSM_STATE SET STAGE = " + operationalId + " WHERE FSM IN (SELECT ID FROM FSM_FINITE_STATE_MACHINE WHERE ID IN (SELECT FSM FROM DLD_DEVICE_LIFE_CYCLE))");
        sql.add("UPDATE FSM_STATE SET STAGE = " + preOperationalId + " WHERE NAME IN ('dlc.default.commissioning', 'dlc.default.inStock')");
        sql.add("UPDATE FSM_STATE SET STAGE = " + postOperationalId + " WHERE NAME IN ('dlc.default.removed', 'dlc.default.decommissioned')");
        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                sql.forEach(sqlCommand -> execute(statement, sqlCommand));
            }
        });
    }

    private Supplier<IllegalStateException> getDeviceStageSetException() {
        return () -> new IllegalStateException("Default end device stage set not installed correctly");
    }
}
