/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.ids.impl;


import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class UpgraderV10_9 implements Upgrader {

    private final IdsService idsService;
    private final DataModel dataModel;

    @Inject
    UpgraderV10_9(IdsService idsService, DataModel dataModel) {
        this.idsService = idsService;
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        boolean offsetAlreadyExist = dataModel.doesColumnExist("IDS_TIMESERIES", "OFFSET_VALUE");
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 9));
        if (!offsetAlreadyExist) {
            try (Connection connection = dataModel.getConnection(true);
                 Statement statement = connection.createStatement()) {
                execute(statement, "UPDATE IDS_TIMESERIES SET OFFSET_VALUE=OFFSET_VALUE*3600");
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        }
    }
}
