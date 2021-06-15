/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class UpgraderV10_9_4 implements Upgrader {
    private final DataModel dataModel;

    @Inject
    UpgraderV10_9_4(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 9, 4));
        try (Connection connection = dataModel.getConnection(true);
             Statement statement = connection.createStatement()) {
            execute(statement, "UPDATE \"DDC_DEVICEPROTOCOLPROPERTY\" SET \"PROPERTYSPEC\" = 'DeviceId' where \"PROPERTYSPEC\" = 'DevideId'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
