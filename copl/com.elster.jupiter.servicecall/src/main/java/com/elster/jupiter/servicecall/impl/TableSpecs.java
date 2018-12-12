/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.SqlDialect;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallLifeCycle;
import com.elster.jupiter.servicecall.ServiceCallLog;
import com.elster.jupiter.servicecall.ServiceCallType;

import java.util.ArrayList;
import java.util.List;

import static com.elster.jupiter.orm.ColumnConversion.CLOB2STRING;
import static com.elster.jupiter.orm.Table.MAX_STRING_LENGTH;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.Version.version;

/**
 * Created by bvn on 2/4/16.
 */
public enum TableSpecs {
    SCS_SERVICE_CALL_LIFECYCLE {
        @Override
        public void addTo(DataModel dataModel, SqlDialect sqlDialect) {
            Table<ServiceCallLifeCycle> table = dataModel.addTable(name(), ServiceCallLifeCycle.class);
            table.map(ServiceCallLifeCycleImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME")
                    .varChar(NAME_LENGTH)
                    .notNull()
                    .map(ServiceCallLifeCycleImpl.Fields.name.fieldName())
                    .add();
            Column finiteStateMachine = table.column("FSM").number().notNull().add();
            table.foreignKey("FK_FSM")
                    .references(FiniteStateMachine.class)
                    .map(ServiceCallLifeCycleImpl.Fields.finiteStateMachine.fieldName())
                    .on(finiteStateMachine)
                    .add();
            table.primaryKey("SCS_PK_SERVICECALL_LC").on(idColumn).add();
        }
    },

    SCS_SERVICE_CALL_TYPE {
        @Override
        public void addTo(DataModel dataModel, SqlDialect sqlDialect) {
            Table<IServiceCallType> table = dataModel.addTable(name(), IServiceCallType.class);
            table.alsoReferredToAs(ServiceCallType.class);
            table.map(ServiceCallTypeImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column name = table.column("NAME")
                    .varChar(NAME_LENGTH)
                    .notNull()
                    .map(ServiceCallTypeImpl.Fields.name.fieldName())
                    .add();
            table.column("HANDLER")
                    .varChar(NAME_LENGTH)
                    .notNull()
                    .map(ServiceCallTypeImpl.Fields.handler.fieldName())
                    .add();
            table.column("LOGLEVEL")
                    .number()
                    .conversion(ColumnConversion.NUMBER2ENUM)
                    .map(ServiceCallTypeImpl.Fields.logLevel.fieldName())
                    .add();
            table.column("STATUS")
                    .number()
                    .conversion(ColumnConversion.NUMBER2ENUM)
                    .map(ServiceCallTypeImpl.Fields.status.fieldName())
                    .add();
            Column versionName = table.column("VERSIONNAME")
                    .varChar(NAME_LENGTH)
                    .notNull()
                    .map(ServiceCallTypeImpl.Fields.versionName.fieldName())
                    .add();
            table.column("CURRENTSTATE")
                    .number()
                    .conversion(ColumnConversion.NUMBER2ENUM)
                    .map(ServiceCallTypeImpl.Fields.currentLifeCycleState.fieldName())
                    .add();
            Column serviceCallLifeCycle = table.column("LIFECYCLE").number().notNull().add();
            table.addAuditColumns();
            table.foreignKey("FK_LIFECYCLE")
                    .references(ServiceCallLifeCycle.class)
                    .map(ServiceCallTypeImpl.Fields.serviceCallLifeCycle.fieldName())
                    .on(serviceCallLifeCycle)
                    .add();
            table.primaryKey("SCT_PK_SERVICECALLTYPE").on(idColumn).add();
            table.unique("SCT_U_TYPE").on(name, versionName).add();
        }
    },
    SCS_CPS_USAGE {
        @Override
        public void addTo(DataModel dataModel, SqlDialect sqlDialect) {
            Table<ServiceCallTypeCustomPropertySetUsage> table = dataModel.addTable(name(), ServiceCallTypeCustomPropertySetUsage.class);
            table.map(ServiceCallTypeCustomPropertySetUsageImpl.class);
            Column serviceCallType = table.column("SERVICECALLTYPE").number().notNull().add();
            Column customPropertySet = table.column("CUSTOMPROPERTYSET").number().notNull().add();
            table.addAuditColumns().forEach(column -> column.since(version(10, 2)));
            table.primaryKey("PK_SCS_CPS_USAGE").on(serviceCallType, customPropertySet).add();
            table.foreignKey("FK_SCS_SERVICECALLTYPE")
                    .references(SCS_SERVICE_CALL_TYPE.name())
                    .on(serviceCallType)
                    .onDelete(DeleteRule.CASCADE)
                    .map(ServiceCallTypeCustomPropertySetUsageImpl.Fields.ServciceCallType.fieldName())
                    .reverseMap(ServiceCallTypeImpl.Fields.customPropertySets.fieldName())
                    .composition()
                    .add();
            table.foreignKey("FK_SCS_CPS")
                    .references(RegisteredCustomPropertySet.class)
                    .on(customPropertySet)
                    .onDelete(DeleteRule.CASCADE)
                    .map(ServiceCallTypeCustomPropertySetUsageImpl.Fields.CustomPropertySet.fieldName())
                    .add();
        }
    },
    SCS_SERVICE_CALL {
        @Override
        public void addTo(DataModel dataModel, SqlDialect sqlDialect) {
            Table<ServiceCall> table = dataModel.addTable(name(), ServiceCall.class);
            table.map(ServiceCallImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column parent = table.column("PARENT")
                    .number()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .add();
            table.column("LASTCOMPLETEDTIME")
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(ServiceCallImpl.Fields.lastCompletedTime.fieldName())
                    .add();
            Column state = table.column("STATE")
                    .number()
                    .add();
            table.column("ORIGIN").varChar(NAME_LENGTH).map(ServiceCallImpl.Fields.origin.fieldName()).add();
            table.column("EXTERNALREFERENCE")
                    .varChar(NAME_LENGTH)
                    .map(ServiceCallImpl.Fields.externalReference.fieldName())
                    .add();
            table.column("REFERENCE")
                    .varChar(NAME_LENGTH)
                    .as("'SC_'||" + sqlDialect.leftPad("ID", ServiceCallImpl.ZEROFILL_SIZE, "0") + ")")
                    .alias("internalReference")
                    .add();
            List<Column> target = table.addRefAnyColumns("TARGET", false, ServiceCallImpl.Fields.targetObject.fieldName());
            Column serviceCallType = table.column("SERVICECALLTYPE").number().notNull().add();
            table.addAuditColumns();

            table.primaryKey("SCS_PK_SERVICECALL").on(idColumn).add();
            table.foreignKey("FK_SCS_SERVICECALL_SCT")
                    .on(serviceCallType)
                    .references(IServiceCallType.class)
                    .map(ServiceCallImpl.Fields.type.fieldName())
                    .add();
            table.foreignKey("FK_SCS_SERVICECALL_PARENT")
                    .references(SCS_SERVICE_CALL.name())
                    .on(parent)
                    .onDelete(DeleteRule.CASCADE)
                    .map(ServiceCallImpl.Fields.parent.fieldName())
                    .add();
            table.foreignKey("FK_SCS_SERVICECALL_STATE")
                    .on(state)
                    .references(State.class)
                    .map(ServiceCallImpl.Fields.state.fieldName())
                    .add();

            List<Column> targetIndexColumns = new ArrayList<>(1 + target.size());
            targetIndexColumns.add(serviceCallType);
            targetIndexColumns.addAll(target);
            table.index("SCS_IX_SCS_TARGET_OBJECT").on(targetIndexColumns.toArray(new Column[targetIndexColumns.size()])).add();
        }
    },
    SCS_SERVICE_CALL_LOG {
        @Override
        public void addTo(DataModel dataModel, SqlDialect sqlDialect) {
            Table<ServiceCallLog> table = dataModel.addTable(name(), ServiceCallLog.class);
            table.map(ServiceCallLogImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column serviceCall = table.column("SERVICECALL").number().notNull().add();
            table.foreignKey("FK_SCS_SERVICECALL_LOG")
                    .references(SCS_SERVICE_CALL.name())
                    .on(serviceCall)
                    .onDelete(DeleteRule.CASCADE)
                    .map(ServiceCallLogImpl.Fields.serviceCall.fieldName())
                    .add();
            table.column("LOGLEVEL")
                    .number()
                    .conversion(ColumnConversion.NUMBER2ENUM)
                    .notNull()
                    .map(ServiceCallLogImpl.Fields.logLevel.fieldName())
                    .add();
            table.column("TIMESTAMP")
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .notNull()
                    .map(ServiceCallLogImpl.Fields.timestamp.fieldName())
                    .add();
            table.column("STACKTRACE")
                    .type("CLOB")
                    .conversion(CLOB2STRING)
                    .map(ServiceCallLogImpl.Fields.stackTrace.fieldName())
                    .add();
            table.column("MESSAGE")
                    .number()
                    .varChar(MAX_STRING_LENGTH)
                    .map(ServiceCallLogImpl.Fields.message.fieldName())
                    .add();
            table.primaryKey("SCS_PK_SERVICECALLLOG").on(idColumn).add();

        }
    };

    public abstract void addTo(DataModel component, SqlDialect sqlDialect);

}
