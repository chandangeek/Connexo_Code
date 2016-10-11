package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.PreferenceType;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

class UpgraderV10_2 implements Upgrader {

    private final DataModel dataModel;

    @Inject
    public UpgraderV10_2(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 2));
        this.upgradeUserPreferences();
    }

    private void upgradeUserPreferences() {
        try (Connection connection = this.dataModel.getConnection(true)) {
            this.upgradeUserPreferences(connection, "SPACE", "-", Integer.toString(PreferenceType.DATETIME_SEPARATOR.ordinal()));
            this.upgradeUserPreferences(connection, "TD", "DT", Integer.toString(PreferenceType.DATETIME_ORDER.ordinal()));
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void upgradeUserPreferences(Connection connection, String formatKey, String formatBE, String formatFE) {
        try (PreparedStatement statement = this.upgradeUserPreferencesStatement(connection, formatKey, formatBE, formatFE)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private PreparedStatement upgradeUserPreferencesStatement(Connection connection, String formatKey, String formatBE, String formatFE) throws
            SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE USR_PREFERENCES SET format_be = ?, format_fe = ? WHERE formatkey = ?");
        statement.setString(1, formatBE);
        statement.setString(2, formatFE);
        statement.setString(3, formatKey);
        return statement;
    }
}
