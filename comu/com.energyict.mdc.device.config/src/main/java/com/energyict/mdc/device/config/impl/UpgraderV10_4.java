/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.energyict.mdc.device.config.events.EventType;

import javax.inject.Inject;
import java.sql.Statement;
import java.util.Arrays;
import java.util.EnumSet;

@LiteralSql
class UpgraderV10_4 implements Upgrader {

    private final DataModel dataModel;
    private final EventService eventService;

    @Inject
    UpgraderV10_4(DataModel dataModel, EventService eventService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 4));
        installNewEventTypes();
        fillDeviceTypesToSecurityAccessorTypesTable();
    }

    private void installNewEventTypes() {
        EnumSet.of(
                EventType.COMTASKENABLEMENT_SWITCH_ON_CONNECTION_FUNCTION,
                EventType.COMTASKENABLEMENT_SWITCH_OFF_CONNECTION_FUNCTION,
                EventType.COMTASKENABLEMENT_SWITCH_FROM_DEFAULT_TO_CONNECTION_FUNCTION,
                EventType.COMTASKENABLEMENT_SWITCH_FROM_TASK_TO_CONNECTION_FUNCTION,
                EventType.COMTASKENABLEMENT_SWITCH_FROM_CONNECTION_FUNCTION_TO_DEFAULT,
                EventType.COMTASKENABLEMENT_SWITCH_FROM_CONNECTION_FUNCTION_TO_TASK,
                EventType.COMTASKENABLEMENT_SWITCH_BETWEEN_CONNECTION_FUNCTIONS)
                .forEach(eventType -> eventType.install(eventService));
    }

    private void fillDeviceTypesToSecurityAccessorTypesTable() {
        executeInTransaction(
                "insert into " + TableSpecs.DTC_SECACCTYPES_ON_DEVICETYPE.name() +
                        " (" + SecurityAccessorTypeOnDeviceTypeImpl.Fields.DEVICETYPE.name() +
                        ", " + SecurityAccessorTypeOnDeviceTypeImpl.Fields.SECACCTYPE.name() +
                        ", VERSIONCOUNT, CREATETIME, MODTIME, USERNAME)" +
                        " select DEVICETYPEID, ID, VERSIONCOUNT, CREATETIME, MODTIME, USERNAME" +
                        " from PKI_SECACCESSORTYPE",
                "insert into " + TableSpecs.Constants.DTC_SECACCTYPES_ON_DEVICETYPE_JOURNAL_TABLE +
                        " (" + SecurityAccessorTypeOnDeviceTypeImpl.Fields.DEVICETYPE.name() +
                        ", " + SecurityAccessorTypeOnDeviceTypeImpl.Fields.SECACCTYPE.name() +
                        ", VERSIONCOUNT, CREATETIME, MODTIME, USERNAME, JOURNALTIME)" +
                        " select DEVICETYPEID, ID, VERSIONCOUNT, CREATETIME, MODTIME, USERNAME, JOURNALTIME" +
                        " from " + com.elster.jupiter.pki.impl.TableSpecs.Constants.PKI_SECACCESSORTYPE_JOURNAL_TABLE,
                "alter table PKI_SECACCESSORTYPE" +
                        " drop column DEVICETYPEID",
                "alter table " + com.elster.jupiter.pki.impl.TableSpecs.Constants.PKI_SECACCESSORTYPE_JOURNAL_TABLE +
                        " drop column DEVICETYPEID"
                );
    }

    private void executeInTransaction(String... sql) {
        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                Arrays.stream(sql).forEach(sqlCommand -> execute(statement, sqlCommand));
            }
        });
    }
}
