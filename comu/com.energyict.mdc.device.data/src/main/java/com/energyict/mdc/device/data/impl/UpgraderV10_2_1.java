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

class UpgraderV10_2_1 implements Upgrader {
    private final DataModel dataModel;

    @Inject
    UpgraderV10_2_1(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        List<String> sql = new ArrayList<>();
        sql.add("UPDATE DDC_DEVICE dev SET dev.NAME = dev.MRID, dev.MRID = (SELECT MRID FROM MTR_ENDDEVICE WHERE AMRSYSTEMID = 1 AND AMRID = dev.ID)");
        sql.add("UPDATE DDC_DEVICEJRNL djrnl SET NAME = MRID, MRID = (SELECT MRID FROM DDC_DEVICE WHERE id = djrnl.id)");

                dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                sql.forEach(sqlCommand -> execute(statement, sqlCommand));
            }
        });

        dataModelUpgrader.upgrade(dataModel, version(10, 2, 1));
    }

    private void execute(Statement statement, String sql) {
        try {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }
}
