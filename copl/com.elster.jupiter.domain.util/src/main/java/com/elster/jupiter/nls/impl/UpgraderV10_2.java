/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

class UpgraderV10_2 implements Upgrader {

    private final DataModel dataModel;

    @Inject
    public UpgraderV10_2(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 2));
        this.upgradeTranslations();
    }

    private void upgradeTranslations() {
        try (Connection connection = this.dataModel.getConnection(true)) {
            this.upgradeTranslations(connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void upgradeTranslations(Connection connection) {
        try (Statement statement = this.upgradeTranslationsStatement(connection)) {
            statement.executeBatch();
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private Statement upgradeTranslationsStatement(Connection connection) throws
            SQLException {
        Statement statement = connection.createStatement();
        statement.addBatch("DELETE FROM NLS_ENTRY");
        statement.addBatch("DELETE FROM NLS_KEY");
        return statement;
    }
}
