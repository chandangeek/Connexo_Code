/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeFail;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeRequest;
import com.elster.jupiter.users.User;

public enum TableSpecs {
    UPE_REQUEST {
        @Override
        void addTo(DataModel dataModel) {
            Table<UsagePointStateChangeRequest> table = dataModel.addTable(this.name(), UsagePointStateChangeRequest.class);
            table.map(UsagePointStateChangeRequestImpl.class);

            Column id = table.addAutoIdColumn();
            Column usagePoint = table.column("USAGE_POINT").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            table.column("TRANSITION_ID").map(UsagePointStateChangeRequestImpl.Fields.TRANSITION_ID.fieldName()).number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            table.column("FROM_STATE_NAME").map(UsagePointStateChangeRequestImpl.Fields.FROM_STATE_NAME.fieldName()).varChar().notNull().add();
            table.column("TO_STATE_NAME").map(UsagePointStateChangeRequestImpl.Fields.TO_STATE_NAME.fieldName()).varChar().notNull().add();
            table.column("TRANSITION_TIME").map(UsagePointStateChangeRequestImpl.Fields.TRANSITION_TIME.fieldName()).number().conversion(ColumnConversion.NUMBER2INSTANT).notNull().add();
            table.column("SCHEDULE_TIME").map(UsagePointStateChangeRequestImpl.Fields.SCHEDULE_TIME.fieldName()).number().conversion(ColumnConversion.NUMBER2INSTANT).notNull().add();
            Column originator = table.column("ORIGINATOR").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            table.column("STATUS").map(UsagePointStateChangeRequestImpl.Fields.STATUS.fieldName()).number().conversion(ColumnConversion.NUMBER2ENUM).notNull().add();
            table.column("FAIL_REASON").map(UsagePointStateChangeRequestImpl.Fields.FAIL_REASON.fieldName()).varChar().add();

            table.primaryKey("PK_UPE_REQUEST").on(id).add();
            table.foreignKey("FK_UPE_REQUEST_2_UP")
                    .on(usagePoint)
                    .references(UsagePoint.class)
                    .map(UsagePointStateChangeRequestImpl.Fields.USAGE_POINT.fieldName())
                    .add();
            table.foreignKey("FK_UPE_REQUEST_2_USER")
                    .on(originator)
                    .references(User.class)
                    .map(UsagePointStateChangeRequestImpl.Fields.ORIGINATOR.fieldName())
                    .add();
        }
    },
    UPE_REQUEST_PROPERTY {
        @Override
        void addTo(DataModel dataModel) {
            Table<UsagePointStateChangePropertyImpl> table = dataModel.addTable(this.name(), UsagePointStateChangePropertyImpl.class);
            table.map(UsagePointStateChangePropertyImpl.class);

            Column changeRequest = table.column("CHANGE_REQUEST").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            Column key = table.column("P_KEY").map(UsagePointStateChangePropertyImpl.Fields.KEY.fieldName()).varChar(Table.MAX_STRING_LENGTH).notNull().add();
            table.column("P_VALUE").map(UsagePointStateChangePropertyImpl.Fields.VALUE.fieldName()).varChar(Table.MAX_STRING_LENGTH).add();

            table.primaryKey("PK_UPE_REQUEST_PROPERTY").on(changeRequest, key).add();
            table.foreignKey("FK_UPE_P_2_REQUEST")
                    .on(changeRequest)
                    .references(UsagePointStateChangeRequest.class)
                    .map(UsagePointStateChangePropertyImpl.Fields.CHANGE_REQUEST.fieldName())
                    .reverseMap(UsagePointStateChangeRequestImpl.Fields.PROPERTIES.fieldName())
                    .composition()
                    .add();
        }
    },
    UPE_REQUEST_FAIL {
        @Override
        void addTo(DataModel dataModel) {
            Table<UsagePointStateChangeFail> table = dataModel.addTable(this.name(), UsagePointStateChangeFail.class);
            table.map(UsagePointStateChangeFailImpl.class);

            Column changeRequest = table.column("CHANGE_REQUEST").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            Column key = table.column("F_KEY").map(UsagePointStateChangeFailImpl.Fields.KEY.fieldName()).varChar(Table.MAX_STRING_LENGTH).notNull().add();
            table.column("F_SOURCE").map(UsagePointStateChangeFailImpl.Fields.FAIL_SOURCE.fieldName()).varChar(Table.NAME_LENGTH).conversion(ColumnConversion.CHAR2ENUM).notNull().add();
            table.column("NAME").map(UsagePointStateChangeFailImpl.Fields.NAME.fieldName()).varChar().notNull().add();
            table.column("MESSAGE").map(UsagePointStateChangeFailImpl.Fields.MESSAGE.fieldName()).varChar().add();

            table.primaryKey("PK_UPE_REQUEST_FAIL").on(changeRequest, key).add();
            table.foreignKey("FK_UPE_F_2_REQUEST")
                    .on(changeRequest)
                    .references(UsagePointStateChangeRequest.class)
                    .map(UsagePointStateChangeFailImpl.Fields.CHANGE_REQUEST.fieldName())
                    .reverseMap(UsagePointStateChangeRequestImpl.Fields.FAILS.fieldName())
                    .composition()
                    .add();
        }
    };

    abstract void addTo(DataModel dataModel);
}
