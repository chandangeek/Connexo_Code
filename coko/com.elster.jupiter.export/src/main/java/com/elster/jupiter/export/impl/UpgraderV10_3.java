/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.MissingDataOption;
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
        upgradeReadingTypeDataSelector();
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

    private void upgradeReadingTypeDataSelector(){
        try (Connection connection = this.dataModel.getConnection(true)) {
            try(PreparedStatement statement = connection.prepareStatement("ALTER TABLE DES_RTDATASELECTOR ADD NEW_EXPORT_COMPLETE NUMBER")){
                statement.executeUpdate();
            }
            try(PreparedStatement statement = connection.prepareStatement("ALTER TABLE DES_RTDATASELECTORJRNL ADD NEW_EXPORT_COMPLETE NUMBER")){
                statement.executeUpdate();
            }
            try(PreparedStatement statement = upgradeReadingTypeTDataSelector(connection, MissingDataOption.EXCLUDE_INTERVAL.ordinal(), 'N')){
                statement.executeUpdate();
            }
            try(PreparedStatement statement = upgradeReadingTypeTDataSelector(connection, MissingDataOption.EXCLUDE_ITEM.ordinal(), 'Y')){
                statement.executeUpdate();
            }
            try(PreparedStatement statement = upgradeReadingTypeTDataSelectorJRNL(connection, MissingDataOption.EXCLUDE_INTERVAL.ordinal(), 'N')){
                statement.executeUpdate();
            }
            try(PreparedStatement statement = upgradeReadingTypeTDataSelectorJRNL(connection, MissingDataOption.EXCLUDE_ITEM.ordinal(), 'Y')){
                statement.executeUpdate();
            }
            try(PreparedStatement statement = connection.prepareStatement("ALTER TABLE DES_RTDATASELECTOR DROP COLUMN EXPORT_COMPLETE")){
                statement.executeUpdate();
            }
            try(PreparedStatement statement = connection.prepareStatement("ALTER TABLE DES_RTDATASELECTORJRNL DROP COLUMN EXPORT_COMPLETE")){
                statement.executeUpdate();
            }
            try(PreparedStatement statement = connection.prepareStatement("ALTER TABLE DES_RTDATASELECTOR RENAME COLUMN NEW_EXPORT_COMPLETE TO EXPORT_COMPLETE")){
                statement.executeUpdate();
            }
            try(PreparedStatement statement = connection.prepareStatement("ALTER TABLE DES_RTDATASELECTORJRNL RENAME COLUMN NEW_EXPORT_COMPLETE TO EXPORT_COMPLETE")){
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private PreparedStatement upgradeReadingTypeTDataSelector(Connection connection, int ordinalValue, char charValue) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE DES_RTDATASELECTOR SET DES_RTDATASELECTOR.NEW_EXPORT_COMPLETE = ? WHERE DES_RTDATASELECTOR.EXPORT_COMPLETE = ?");
        statement.setInt(1, ordinalValue);
        statement.setString(2, String.valueOf(charValue));
        return statement;
    }

    private PreparedStatement upgradeReadingTypeTDataSelectorJRNL(Connection connection, int ordinalValue, char charValue) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE DES_RTDATASELECTORJRNL SET DES_RTDATASELECTORJRNL.NEW_EXPORT_COMPLETE = ? WHERE DES_RTDATASELECTORJRNL.EXPORT_COMPLETE = ?");
        statement.setInt(1, ordinalValue);
        statement.setString(2, String.valueOf(charValue));
        return statement;
    }
}
