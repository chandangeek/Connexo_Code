package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.impl.CreateLocationMemberTableOperation;
import com.elster.jupiter.metering.impl.GeoCoordinatesSpatialMetaDataTableOperation;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.sql.SQLException;
import java.sql.Statement;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_2 implements Upgrader {

    private static final Version VERSION = version(10, 2);
    private final DataModel dataModel;
    private final MeteringService meteringService;

    @Inject
    public UpgraderV10_2(DataModel dataModel, MeteringService meteringService) {
        this.dataModel = dataModel;
        this.meteringService = meteringService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, VERSION);
        ImmutableList<String> sql = ImmutableList.of(
                "UPDATE MTR_USAGEPOINTDETAIL SET GROUNDED = 'YES' where MTR_USAGEPOINTDETAIL.GROUNDED = 'Y'",
                "UPDATE MTR_USAGEPOINTDETAIL SET GROUNDED = 'NO' where MTR_USAGEPOINTDETAIL.GROUNDED = 'N'",
                "UPDATE MTR_USAGEPOINTDETAILJRNL SET GROUNDED = 'YES' where MTR_USAGEPOINTDETAILJRNL.GROUNDED = 'Y'",
                "UPDATE MTR_USAGEPOINTDETAILJRNL SET GROUNDED = 'NO' where MTR_USAGEPOINTDETAILJRNL.GROUNDED = 'N'"
        );

        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                sql.forEach(sqlCommand -> execute(statement, sqlCommand));
            }
        });
        new CreateLocationMemberTableOperation(dataModel, meteringService.getLocationTemplate()).execute();
        new GeoCoordinatesSpatialMetaDataTableOperation(dataModel).execute();
        meteringService.createLocationTemplate();
    }

    private void execute(Statement statement, String sql) {
        try {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }


}

