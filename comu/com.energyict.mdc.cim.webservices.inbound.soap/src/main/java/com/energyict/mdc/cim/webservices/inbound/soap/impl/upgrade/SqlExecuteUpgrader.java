
/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl.upgrade;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.upgrade.Upgrader;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.InboundSoapEndpointsActivator;

import java.sql.Statement;
import java.util.List;

/**
 * This class executes a list of SQL commands as upgrade
 *
 */
abstract class SqlExecuteUpgrader implements Upgrader {

    private final OrmService ormService;

    SqlExecuteUpgrader(OrmService ormService) {
        this.ormService = ormService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        ormService.getDataModel(MeteringService.COMPONENTNAME).get()
                .useConnectionRequiringTransaction(connection -> {
                    try (Statement statement = connection.createStatement()) {
                        getSQLStatementsToExecute().forEach(sqlCommand -> execute(statement, sqlCommand));
                    }
                });
    }

    protected abstract List<String> getSQLStatementsToExecute();

    protected String ignoreColumnExistsOrTableDoesNotExist(String sql) {
        StringBuilder builder = new StringBuilder("declare column_exists exception;");
        builder.append("table_does_not_exist exception;");
        builder.append("pragma exception_init (column_exists , -01430);");
        builder.append("pragma exception_init (table_does_not_exist , -00942);");
        builder.append("begin execute immediate '");
        builder.append(sql);
        builder.append("';exception when column_exists then null;");
        builder.append("when table_does_not_exist then null; end;");
        return builder.toString();
    }
}
