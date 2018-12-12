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
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.streams.FancyJoiner;

import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class UpgraderV10_3 implements Upgrader {
    private final DataModel dataModel;
    private final FiniteStateMachineService finiteStateMachineService;
    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private List<String> finalsql = new ArrayList<>();

    @Inject
    public UpgraderV10_3(DataModel dataModel, FiniteStateMachineService finiteStateMachineService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.dataModel = dataModel;
        this.finiteStateMachineService = finiteStateMachineService;
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        List<String> sql = new ArrayList<>();
        removeMicroAction();
        StageSet stageSet = finiteStateMachineService.findStageSetByName(MeteringService.END_DEVICE_STAGE_SET_NAME).orElseThrow(getDeviceStageSetException());
        long preOperationalId = stageSet.getStageByName(EndDeviceStage.PRE_OPERATIONAL.getKey()).orElseThrow(getDeviceStageSetException()).getId();
        long operationalId = stageSet.getStageByName(EndDeviceStage.OPERATIONAL.getKey()).orElseThrow(getDeviceStageSetException()).getId();
        long postOperationalId = stageSet.getStageByName(EndDeviceStage.POST_OPERATIONAL.getKey()).orElseThrow(getDeviceStageSetException()).getId();

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

    private void removeMicroAction() {
        List<Long> ids = deviceLifeCycleConfigurationService.findAllDeviceLifeCycles().stream()
                .map(DeviceLifeCycle::getAuthorizedActions)
                .flatMap(Collection::stream)
                .filter(authorizedAction -> AuthorizedTransitionAction.class.isAssignableFrom(authorizedAction.getClass()))
                .map(authorizedAction -> (AuthorizedTransitionAction) authorizedAction)
                .map(HasId::getId)
                .collect(Collectors.toList());

        removeActions(ids);
    }

    private void removeActions( List<Long>  ids) {
        String sql = "SELECT ID, ACTIONBITS FROM DLD_AUTHORIZED_ACTION WHERE ID in " + ids.stream().collect(FancyJoiner.joining("," ,"", "(", ")"));
        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                ResultSet rs = statement.executeQuery(sql);
                while (rs.next()) {
                    calculateNewActionBits(rs.getLong(1), rs.getLong(2));
                }
                finalsql.stream().forEach(finalSQL -> {
                    try {
                        statement.execute(finalSQL);
                    } catch (SQLException e) {
                        throw new UnderlyingSQLFailedException(e);
                    }
                });
            } catch (SQLException e) {
                Logger.getLogger(this.getClass().getSimpleName()).log(Level.SEVERE, e.getMessage());
                throw new UnderlyingSQLFailedException(e);
            }
        });
    }

    private void calculateNewActionBits(long id, long actionBits) throws SQLException {
        long first5Cached = actionBits & 63L;
        actionBits = actionBits >> 1L;
        for(long i = 0; i <= 5; i++) {
            actionBits &= ~(1L << i);
        }
        actionBits |= first5Cached;
        String sql = "UPDATE DLD_AUTHORIZED_ACTION SET ACTIONBITS=" + actionBits + " WHERE ID = " + id;
        finalsql.add(sql);
    }
}
