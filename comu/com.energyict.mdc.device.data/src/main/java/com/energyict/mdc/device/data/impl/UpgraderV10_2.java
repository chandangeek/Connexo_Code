package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.elster.jupiter.orm.Version.version;

class UpgraderV10_2 implements Upgrader {

    private final DataModel dataModel;

    @Inject
    UpgraderV10_2(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 2));

        List<String> sql = new ArrayList<>();
        sql.add("UPDATE DDC_DEVICE SET ESTIMATION_ACTIVE = 'Y' where ID IN (SELECT DEVICE FROM DDC_DEVICEESTACTIVATION WHERE ACTIVE = 'Y')");
        sql.add("UPDATE (SELECT t.id, t. batch_id, s.BATCHID from DDC_DEVICE t, DDC_DEVICEINBATCH s where t.ID = s.DEVICEID) SET BATCH_ID = BATCHID");
        sql.add("UPDATE DDC_CONNECTIONTASK SET simultaneousconnections=1 where simultaneousconnections = 0");
        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                sql.forEach(sqlCommand -> execute(statement, sqlCommand));
            }
        });
    }

    private void execute(Statement statement, String sql) {
        try {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }
}
