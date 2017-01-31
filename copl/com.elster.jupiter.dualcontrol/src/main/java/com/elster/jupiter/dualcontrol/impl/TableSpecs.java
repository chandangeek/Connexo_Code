/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dualcontrol.impl;

import com.elster.jupiter.dualcontrol.Monitor;
import com.elster.jupiter.dualcontrol.UserOperation;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.users.User;

public enum TableSpecs {

    DUC_MONITOR {
        @Override
        void addTo(DataModel dataModel) {
            Table<Monitor> table = dataModel.addTable(name(), Monitor.class);
            table.map(MonitorImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("STATE")
                    .varChar(18)
                    .notNull()
                    .conversion(ColumnConversion.CHAR2ENUM)
                    .map(MonitorImpl.Fields.STATE.fieldName())
                    .add();
            table.setJournalTableName("DUC_MONITORJRNL");
            table.primaryKey("DUC_PK_MONITOR")
                    .on(idColumn)
                    .add();
        }
    },
    DUC_USER_OPERATION {
        @Override
        void addTo(DataModel dataModel) {
            Table<UserOperation> table = dataModel.addTable(name(), UserOperation.class);
            table.map(UserOperationImpl.class);
            Column monitorColumn = table.column("MONITOR")
                    .number()
                    .notNull()
                    .add();
            Column positionColumn = table.addPositionColumn();
            table.column("USER_ACTION")
                    .varChar(7)
                    .notNull()
                    .conversion(ColumnConversion.CHAR2ENUM)
                    .map(UserOperationImpl.Fields.USER_ACTION.fieldName())
                    .add();
            Column userColumn = table.column("ACTION_USER")
                    .number()
                    .notNull()
                    .add();
            table.setJournalTableName("DUC_USER_OPERATIONJRNL");
            table.primaryKey("DUC_PK_USER_OPERATION")
                    .on(monitorColumn, positionColumn)
                    .add();
            table.foreignKey("DUC_OPERATION_IN_MONITOR")
                    .references(Monitor.class)
                    .on(monitorColumn)
                    .map(UserOperationImpl.Fields.MONITOR.fieldName())
                    .composition()
                    .reverseMap(MonitorImpl.Fields.OPERATIONS.fieldName())
                    .reverseMapOrder(UserOperationImpl.Fields.POSITION.fieldName())
                    .add();
            table.foreignKey("DUC_USER_OF_OPERATION")
                    .references(User.class)
                    .on(userColumn)
                    .map(UserOperationImpl.Fields.USER.fieldName())
                    .add();
        }
    },
    ;

    abstract void addTo(DataModel dataModel);

}
