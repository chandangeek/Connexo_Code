package com.energyict.mdc.device.lifecycle.config.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UpgraderV10_2 implements Upgrader {
    private final DataModel dataModel;

    @Inject
    public UpgraderV10_2(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        List<String> sql = new ArrayList<>();
        sql.add("UPDATE DLD_AUTHORIZED_ACTION SET DISCRIMINATOR = 0 WHERE DISCRIMINATOR = 1");
        sql.add("UPDATE DLD_DEVICE_LIFE_CYCLE SET MAXFUTUREEFFTIMESHIFTVALUE = 1, MAXPASTEFFTIMESHIFTVALUE = 30 WHERE FSM IN (SELECT ID FROM FSM_FINITE_STATE_MACHINE WHERE NAME = 'dlc.standard.device.life.cycle')");
        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                sql.forEach(sqlCommand -> execute(statement, sqlCommand));
            }
        });
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 2));
    }
}
