/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_3 implements Upgrader {


    private final DataModel dataModel;

    @Inject
    UpgraderV10_3(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 3));

        upgradeStandardSelectors();
    }

    private void upgradeStandardSelectors() {
        try (Connection connection = this.dataModel.getConnection(true)) {
            try (PreparedStatement statement = upgradeDataSelector(connection, MeterReadingSelectorConfigImpl.IMPLEMENTOR_NAME, DataExportService.STANDARD_READINGTYPE_DATA_SELECTOR)) {
                statement.executeUpdate();
            }
            try (PreparedStatement statement = upgradeDataSelectorJrnl(connection, MeterReadingSelectorConfigImpl.IMPLEMENTOR_NAME, DataExportService.STANDARD_READINGTYPE_DATA_SELECTOR)) {
                statement.executeUpdate();
            }
            try (PreparedStatement statement = upgradeDataSelector(connection, EventSelectorConfigImpl.IMPLEMENTOR_NAME, DataExportService.STANDARD_EVENT_DATA_SELECTOR)) {
                statement.executeUpdate();
            }
            try (PreparedStatement statement = upgradeDataSelectorJrnl(connection, EventSelectorConfigImpl.IMPLEMENTOR_NAME, DataExportService.STANDARD_EVENT_DATA_SELECTOR)) {
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private PreparedStatement upgradeDataSelector(Connection connection, String implementor, String selector) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE DES_RTDATASELECTOR SET TYPE = ? WHERE EXPORTTASK IN (SELECT ID FROM DES_DATAEXPORTTASK WHERE DATASELECTOR = ?)");
        statement.setString(1, implementor);
        statement.setString(2, selector);
        return statement;
    }

    private PreparedStatement upgradeDataSelectorJrnl(Connection connection, String implementor, String selector) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE DES_RTDATASELECTORJRNL SET TYPE = ? WHERE EXPORTTASK IN (SELECT DISTINCT ID FROM DES_DATAEXPORTTASKJRNL WHERE DATASELECTOR = ?)");
        statement.setString(1, implementor);
        statement.setString(2, selector);
        return statement;
    }
}
