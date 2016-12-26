package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.impl.CreateLocationMemberTableOperation;
import com.elster.jupiter.metering.impl.DefaultTranslationKey;
import com.elster.jupiter.metering.impl.GeoCoordinatesSpatialMetaDataTableOperation;
import com.elster.jupiter.metering.impl.InstallerV10_2Impl;
import com.elster.jupiter.metering.impl.MeteringDataModelService;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;

import com.google.common.collect.ImmutableList;
import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumSet;
import java.util.logging.Logger;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_2 implements Upgrader {

    private static final Version VERSION = version(10, 2);
    private final BundleContext bundleContext;
    private final DataModel dataModel;
    private final ServerMeteringService meteringService;
    private final EventService eventService;
    private final InstallerV10_2Impl installerV10_2;
    private final UserService userService;

    @Inject
    public UpgraderV10_2(BundleContext bundleContext, DataModel dataModel, ServerMeteringService meteringService, EventService eventService, InstallerV10_2Impl installerV10_2, UserService userService) {
        this.bundleContext = bundleContext;
        this.dataModel = dataModel;
        this.meteringService = meteringService;
        this.eventService = eventService;
        this.installerV10_2 = installerV10_2;
        this.userService = userService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModel.useConnectionRequiringTransaction(connnection -> {
            try (Statement statement = connnection.createStatement()) {
                ImmutableList.of(
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
                        "alter table MTR_MULTIPLIERTYPE drop PRIMARY KEY drop index",
                        "alter table MTR_MULTIPLIERTYPE add constraint MTR_PK_MULTIPLIERTYPE primary key(ID)",
                        "CREATE TABLE MTR_CHANNEL_CONTAINER (ID NUMBER NOT NULL, CONTAINER_TYPE VARCHAR2(80 BYTE) NOT NULL, METER_ACTIVATION NUMBER, EFFECTIVE_CONTRACT NUMBER, VERSIONCOUNT NUMBER NOT NULL, CREATETIME NUMBER NOT NULL, MODTIME NUMBER NOT NULL, USERNAME VARCHAR2(80 CHAR) NOT NULL, CONSTRAINT MTR_CONTRACT_CHANNEL_PK PRIMARY KEY (ID), CONSTRAINT MTR_CH_CONTAINER_MA_UQ UNIQUE (METER_ACTIVATION), CONSTRAINT MTR_CH_CONTAINER_EF_CONTR_UK UNIQUE (EFFECTIVE_CONTRACT), CONSTRAINT MTR_CH_CONTAINER_2_MA FOREIGN KEY (METER_ACTIVATION) REFERENCES MTR_METERACTIVATION (ID))",
                        "INSERT INTO MTR_CHANNEL_CONTAINER (ID, CONTAINER_TYPE, METER_ACTIVATION, VERSIONCOUNT, CREATETIME, MODTIME, USERNAME) SELECT ID, 'MeterActivation', ID, 1, CREATETIME, MODTIME, USERNAME FROM MTR_METERACTIVATION"
                ).forEach(command -> execute(statement, command));
            }
            createChannelsContainerSequence(connnection);
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

        installNewEventTypes();
        new GasDayOptionsCreator(this.meteringService).createIfMissing(this.bundleContext);
        installerV10_2.install(dataModelUpgrader, Logger.getLogger(UpgraderV10_2.class.getName()));
        this.upgradeSubscriberSpecs();


        ImmutableList<String> privilegeRenameUpgradeSql = ImmutableList.of(
                "delete from USR_PRIVILEGEINGROUP where privilegename in (select name from usr_privilege where resourceid in (select id from USR_RESOURCE where name in ('metering.usage.points','metering.reading.types')))",
                "delete from USR_PRIVILEGE where resourceid in (select id from USR_RESOURCE where name in ('metering.usage.points','metering.reading.types'))",
                "delete from USR_RESOURCE where name in ('metering.usage.points','metering.reading.types')",
                "UPDATE MTR_USAGEPOINTDETAILJRNL SET GROUNDED = 'NO' where MTR_USAGEPOINTDETAILJRNL.GROUNDED = 'N'"
        );
        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                privilegeRenameUpgradeSql.forEach(sqlCommand -> execute(statement, sqlCommand));
            }
        });
        userService.addModulePrivileges(installerV10_2);
    }

    private void createChannelsContainerSequence(Connection connection) {
        try (CallableStatement statement = connection.prepareCall("DECLARE SEQ_START NUMBER;\n" +
                "BEGIN SELECT (MAX(ID) + 1) INTO SEQ_START FROM MTR_METERACTIVATION;\n" +
                "IF SEQ_START IS NULL THEN SEQ_START := 1;  END IF;\n" +
                "  EXECUTE IMMEDIATE 'CREATE SEQUENCE MTR_CHANNEL_CONTAINERID START WITH ' || SEQ_START || ' INCREMENT BY 1 CACHE 1000';\n" +
                "END;")) {
            statement.execute();
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void installNewEventTypes() {
        EnumSet.of(EventType.METER_ACTIVATED,
                EventType.METROLOGYCONFIGURATION_CREATED,
                EventType.METROLOGYCONFIGURATION_UPDATED,
                EventType.METROLOGYCONFIGURATION_DELETED,
                EventType.METROLOGY_PURPOSE_DELETED,
                EventType.READING_TYPE_DELIVERABLE_CREATED,
                EventType.READING_TYPE_DELIVERABLE_UPDATED,
                EventType.READING_TYPE_DELIVERABLE_DELETED)
                .forEach(eventType -> eventType.install(eventService));
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
        PreparedStatement statement = connection.prepareStatement("UPDATE MSG_SUBSCRIBERSPEC SET nls_component = ?, nls_layer = ? WHERE name = ?");
        statement.setString(1, MeteringDataModelService.COMPONENT_NAME);
        statement.setString(2, Layer.DOMAIN.name());
        statement.setString(3, DefaultTranslationKey.SWITCH_STATE_MACHINE_SUBSCRIBER.getKey());
        return statement;
    }

}

