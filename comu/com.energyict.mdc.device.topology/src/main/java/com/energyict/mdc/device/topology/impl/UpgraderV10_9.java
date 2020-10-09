package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import static com.elster.jupiter.util.streams.Currying.perform;

public class UpgraderV10_9 implements Upgrader {
    private final DataModel dataModel;

    @Inject
    public UpgraderV10_9(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        try (
                Connection connection = this.dataModel.getConnection(true);
                Statement statement = connection.createStatement();
        ) {
            upgradeActionsSQL()
                    .forEach(sqlCommand-> this.execute(statement, sqlCommand));
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
        dataModelUpgrader.upgrade(this.dataModel, Version.version(10, 9));

    }

    private List<String> upgradeActionsSQL() {
        return Arrays.asList(
                "UPDATE DTL_COMPATHSEGMENT SET NEXTHOPDEVICE = TARGETDEVICE WHERE NEXTHOPDEVICE IS NULL"
        );
    }
}
