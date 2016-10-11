package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.data.DeviceDataServices;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static com.elster.jupiter.orm.Version.version;

class UpgraderV10_2 implements Upgrader {

    private final DataModel dataModel;
    private final InstallerV10_2Impl installerV10_2;
    private final UserService userService;

    @Inject
    UpgraderV10_2(DataModel dataModel, InstallerV10_2Impl installerV10_2, UserService userService) {
        this.dataModel = dataModel;
        this.installerV10_2 = installerV10_2;
        this.userService = userService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        List<String> sql = new ArrayList<>();
        sql.add("ALTER TABLE DDC_DEVICEESTRULESETACTIVATION ADD DEVICE NUMBER");
        sql.add("UPDATE DDC_DEVICEESTRULESETACTIVATION SET DEVICE = ESTIMATIONACTIVATION");
        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                sql.forEach(sqlCommand -> execute(statement, sqlCommand));
            }
        });

        dataModelUpgrader.upgrade(dataModel, version(10, 2));

        sql.clear();
        sql.add("UPDATE DDC_CONNECTIONTASK SET simultaneousconnections = '1' where simultaneousconnections = '0'");
        sql.add("UPDATE DDC_DEVICE SET ESTIMATION_ACTIVE = 'Y' where ID IN (SELECT DEVICE FROM DDC_DEVICEESTACTIVATION WHERE ACTIVE = 'Y')");
        sql.add("UPDATE (SELECT t.id, t. batch_id, s.BATCHID from DDC_DEVICE t, DDC_DEVICEINBATCH s where t.ID = s.DEVICEID) SET BATCH_ID = BATCHID");
        sql.add("UPDATE DDC_DEVICE d SET d.METERID = (SELECT ed.ID FROM MTR_ENDDEVICE ed where d.ID = ed.AMRID)");
        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                sql.forEach(sqlCommand -> execute(statement, sqlCommand));
            }
        });
        this.upgradeSubscriberSpecs();
        installerV10_2.install(dataModelUpgrader, Logger.getLogger(UpgraderV10_2.class.getName()));
        userService.addModulePrivileges(installerV10_2);
    }

    private void execute(Statement statement, String sql) {
        try {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void upgradeSubscriberSpecs() {
        try (Connection connection = this.dataModel.getConnection(true)) {
            this.upgradeSubscriberSpecs(connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void upgradeSubscriberSpecs(Connection connection) {
        try (PreparedStatement statement = this.upgradeSubscriberSpecsStatement(connection)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private PreparedStatement upgradeSubscriberSpecsStatement(Connection connection) throws SQLException {
        SqlBuilder sqlBuilder = new SqlBuilder("UPDATE MSG_SUBSCRIBERSPEC SET nls_component =");
        sqlBuilder.addObject(DeviceDataServices.COMPONENT_NAME);
        sqlBuilder.append(", nls_layer =");
        sqlBuilder.addObject(Layer.DOMAIN.name());
        sqlBuilder.append("where name in (");
        new SubscriberTranslationKeyAppender().appendTo(sqlBuilder);
        sqlBuilder.append(")");
        return sqlBuilder.prepare(connection);
    }

    private class SubscriberTranslationKeyAppender {
        private boolean notFirst = false;

        void appendTo(SqlBuilder sqlBuilder) {
            Stream.of(SubscriberTranslationKeys.values()).forEach(subscriberTranslationKey -> this.appendTo(sqlBuilder, subscriberTranslationKey));
        }

        private void appendTo(SqlBuilder sqlBuilder, SubscriberTranslationKeys key) {
            if (notFirst) {
                sqlBuilder.append(", ");
            }
            sqlBuilder.addObject(key.getKey());
            notFirst = true;
        }

    }
}
