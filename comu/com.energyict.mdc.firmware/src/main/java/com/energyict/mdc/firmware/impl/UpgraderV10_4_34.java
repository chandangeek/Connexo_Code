/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class UpgraderV10_4_34 implements Upgrader {
    private final DataModel dataModel;

    @Inject
    UpgraderV10_4_34(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        try (Connection connection = dataModel.getConnection(true);

        Statement statement = connection.createStatement()) {
            System.out.println("Cleaning manually cancelled firmware campaigns...");

            execute(statement, "update FWC_FC1_CAMPAIGN fc set MANUALLY_CANCELLED = 'N' " +
                    "where MANUALLY_CANCELLED = 'Y' and exists (" +
                    "select sc.ID from SCS_SERVICE_CALL sc join FSM_STATE st on sc.STATE = st.ID " +
                    "where sc.ID = fc.SERVICECALL " +
                    "and st.NAME != 'sclc.default.cancelled')");

            System.out.println("done.");

        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }


}
