/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class UpgraderV10_9_19 implements Upgrader {

    private final DataModel dataModel;
    private final Installer installer;

    private static final String OLD_CUSTOM_PROPERTY_SET_ID = "com.energyict.mdc.sap.soap.custom.eventhandlers.SAPDeviceEventMappingStatusCustomPropertySet";
    private static final String NEW_CUSTOM_PROPERTY_SET_ID = "com.energyict.mdc.sap.soap.webservices.impl.events.SAPDeviceEventMappingStatusCustomPropertySet";

    @Inject
    public UpgraderV10_9_19(DataModel dataModel, ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService, Installer installer) {
        this.dataModel = dataModel;
        this.installer = installer;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 9, 19));
        installer.createMessageHandlers();
        migrateSql();
    }

    private void migrateSql() {
        try (Connection connection = this.dataModel.getConnection(true); Statement statement = connection.createStatement()) {
            execute(statement, "rename CSE_EVENT_MAPPING_STATUS TO SDE_EVENT_MAPPING_STATUS");
            execute(statement, "rename CSE_EVENT_MAPPING_STATUSJRNL TO SDE_EVENT_MAPPING_STATUSJRNL");
            execute(statement, "update CPS_REGISTERED_CUSTOMPROPSET set LOGICALID = '" + NEW_CUSTOM_PROPERTY_SET_ID + "' where LOGICALID = '" + OLD_CUSTOM_PROPERTY_SET_ID + "'");
            execute(statement, "update CPS_REG_CUSTOMPROPSET_JRNL set LOGICALID = '" + NEW_CUSTOM_PROPERTY_SET_ID + "' where LOGICALID = '" + OLD_CUSTOM_PROPERTY_SET_ID + "'");
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }
}