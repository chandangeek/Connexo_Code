package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.metering.impl.CreateLocationMemberTableOperation;
import com.elster.jupiter.metering.impl.GeoCoordinatesSpatialMetaDataTableOperation;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import com.google.common.collect.ImmutableList;
import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import java.sql.SQLException;
import java.sql.Statement;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_2 implements Upgrader {

    private static final Version VERSION = version(10, 2);
    private final BundleContext bundleContext;
    private final DataModel dataModel;
    private final ServerMeteringService meteringService;

    @Inject
    public UpgraderV10_2(BundleContext bundleContext, DataModel dataModel, ServerMeteringService meteringService) {
        this.bundleContext = bundleContext;
        this.dataModel = dataModel;
        this.meteringService = meteringService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModel.useConnectionRequiringTransaction(connnection -> {
            try (Statement statement = connnection.createStatement()) {
                ImmutableList<String> ddl = ImmutableList.of(
                        "create sequence MTR_MULTIPLIERTYPEID start with 1 cache 1000",
                        "alter table MTR_MULTIPLIERTYPE add (ID number)",
                        "update MTR_MULTIPLIERTYPE set ID = MTR_MULTIPLIERTYPEID.NEXTVAL",
                        "alter table MTR_MULTIPLIERTYPE modify (ID number not null)",
                        "alter table MTR_RT_METER_CONFIG drop constraint MTR_FK_RTMC_MULTTP",
                        "alter table MTR_RT_METER_CONFIG add (TEMP number)",
                        "update MTR_RT_METER_CONFIG set TEMP = (select ID from MTR_MULTIPLIERTYPE where MULTIPLIERTYPE = NAME) where MULTIPLIERTYPE is not null",
                        "alter table MTR_RT_METER_CONFIG drop column MULTIPLIERTYPE",
                        "alter table MTR_RT_METER_CONFIG rename column TEMP to MULTIPLIERTYPE",
                        "alter table MTR_RT_UP_CONFIG drop constraint MTR_FK_RTUPC_MULTTP",
                        "alter table MTR_RT_UP_CONFIG add (TEMP number)",
                        "update MTR_RT_UP_CONFIG set TEMP = (select ID from MTR_MULTIPLIERTYPE where MULTIPLIERTYPE = NAME) where MULTIPLIERTYPE is not null",
                        "alter table MTR_RT_UP_CONFIG drop column MULTIPLIERTYPE",
                        "alter table MTR_RT_UP_CONFIG rename column TEMP to MULTIPLIERTYPE",
                        "alter table MTR_MULTIPLIERVALUE drop constraint MTR_FK_MULTIPLIERVALUE_TP",
                        "alter table MTR_MULTIPLIERVALUE add (TEMP number)",
                        "update MTR_MULTIPLIERVALUE set TEMP = (select ID from MTR_MULTIPLIERTYPE where MULITPLIERTYPE = NAME) where MULITPLIERTYPE is not null",
                        "alter table MTR_MULTIPLIERVALUE drop column MULITPLIERTYPE cascade constraints",
                        "alter table MTR_MULTIPLIERVALUE rename column TEMP to MULTIPLIERTYPE",
                        "alter table MTR_MULTIPLIERTYPE drop constraint MTR_PK_MULTIPLIERTYPE",
                        "alter table MTR_MULTIPLIERTYPE add constraint MTR_PK_MULTIPLIERTYPE primary key(ID)"
                );

                ddl.forEach(command -> {
                    try {
                        statement.execute(command);
                    } catch (SQLException e) {
                        throw new UnderlyingSQLFailedException(e);
                    }
                });
            }
        });

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

        new GasDayOptionsCreator(this.meteringService).createIfMissing(this.bundleContext);
    }

    private void execute(Statement statement, String sql) {
        try {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }


}

