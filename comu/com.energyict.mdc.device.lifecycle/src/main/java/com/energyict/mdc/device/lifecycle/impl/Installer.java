/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl;

import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.energyict.mdc.device.lifecycle.DefaultMicroCheck;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleUpdater;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@LiteralSql
public class Installer implements FullInstaller {
    private final DataModel dataModel;
    private final DeviceLifeCycleService deviceLifeCycleService;
    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    @Inject
    Installer(DataModel dataModel, DeviceLifeCycleService deviceLifeCycleService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.dataModel = dataModel;
        this.deviceLifeCycleService = deviceLifeCycleService;
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());

        doTry("Install transition checks", this::installChecksForTransitions, logger);
    }

    private void installChecksForTransitions() {
        if (isCheckbitsColumnPresent()) {
            upgradeTransitionChecks(); // case of upgrade from an older version
        } else {
            installChecksOnDefaultLifecycle(); // case of installation from scratch
        }
    }

    private boolean isCheckbitsColumnPresent() {
        try (Connection connection = dataModel.getConnection(false);
             ResultSet resultSet = connection.getMetaData().getColumns(null, null, "DLD_AUTHORIZED_ACTION", "CHECKBITS")) {
            return resultSet.next();
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void installChecksOnDefaultLifecycle() {
        deviceLifeCycleConfigurationService.findDefaultDeviceLifeCycle()
                .ifPresent(lifeCycle -> {
                    DeviceLifeCycleUpdater updater = lifeCycle.startUpdate();
                    lifeCycle.getFiniteStateMachine().getTransitions()
                            .forEach(transition -> updater.transitionAction(transition).setChecks(applicableChecksFor(transition)).complete());
                    updater.complete().save();
                });
    }

    private Set<String> applicableChecksFor(StateTransition transition) {
        return deviceLifeCycleConfigurationService.getMicroChecks()
                .stream()
                .filter(microCheck -> microCheck.isApplicableForTransition(transition.getFrom(), transition.getTo()))
                .map(MicroCheck::getKey)
                .collect(Collectors.toSet());
    }

    private void upgradeTransitionChecks() {
        String insertInto = "insert into DLD_TRANSITION_CHECKS (TRANSITION, MICRO_CHECK) ";
        String insertion = getCheckBitsByTransitionIds().entrySet().stream()
                .flatMap(this::getValuesForTransition)
                .map(values -> "select " + values + " from dual")
                .collect(Collectors.joining(" union all "));
        if (!insertion.isEmpty()) {
            execute(dataModel,
                    insertInto + insertion,
                    "alter table DLD_AUTHORIZED_ACTION drop column CHECKBITS");
        }
    }

    private Stream<String> getValuesForTransition(Map.Entry<Long, Long> transitionIdAndCheckBits) {
        String transitionId = Long.toString(transitionIdAndCheckBits.getKey());
        long checkBits = transitionIdAndCheckBits.getValue();
        EnumSet<DefaultMicroCheck> checks = EnumSet.noneOf(DefaultMicroCheck.class);
        int mask = 1;
        for (DefaultMicroCheck microCheck : DefaultMicroCheck.values()) {
            if ((checkBits & mask) != 0) {
                // The bit corresponding to the current microCheck is set so add it to the set.
                checks.add(microCheck);
            }
            mask *= 2;
        }
        checks.add(DefaultMicroCheck.METROLOGY_CONFIGURATION_IN_CORRECT_STATE_IF_ANY); // previously was always executed bypassing transition configuration
        return checks.stream()
                .map(deviceLifeCycleService::getKey)
                .map(key -> transitionId + ", '" + key + "'");
    }

    private Map<Long, Long> getCheckBitsByTransitionIds() {
        return executeQuery(dataModel, "select ID, CHECKBITS from DLD_AUTHORIZED_ACTION", this::convertResultSetToMap);
    }

    private Map<Long, Long> convertResultSetToMap(ResultSet resultSet) throws SQLException {
        Map<Long, Long> map = new HashMap<>();
        while (resultSet.next()) {
            map.put(resultSet.getLong(1), resultSet.getLong(2));
        }
        return map;
    }
}
