package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallLifeCycle;
import com.elster.jupiter.servicecall.ServiceCallType;

import static com.elster.jupiter.orm.Table.NAME_LENGTH;

/**
 * Created by bvn on 2/4/16.
 */
public enum TableSpecs {
    SCS_SERVICE_CALL_LIFECYCLE(ServiceCallLifeCycle.class) {
        @Override
        void describeTable(Table table) {
            table.map(ServiceCallLifeCycleImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").varChar(NAME_LENGTH).notNull().map(ServiceCallLifeCycleImpl.Fields.name.fieldName()).add();
            Column finiteStateMachine = table.column("FSM").number().notNull().add();
            table.foreignKey("FK_FSM")
                    .references(FiniteStateMachine.class)
                    .map(ServiceCallLifeCycleImpl.Fields.finiteStateMachine.fieldName())
                    .on(finiteStateMachine)
                    .add();
            table.primaryKey("SCS_PK_SERVICECALL_LC").on(idColumn).add();
        }
    },

    SCS_SERVICE_CALL_TYPE(IServiceCallType.class) {
        @Override
        void describeTable(Table table) {
            table.map(ServiceCallTypeImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar(NAME_LENGTH).notNull().map(ServiceCallTypeImpl.Fields.name.fieldName()).add();
            table.column("LOGLEVEL").number().conversion(ColumnConversion.NUMBER2ENUM).map(ServiceCallTypeImpl.Fields.logLevel.fieldName()).add();
            table.column("STATUS").number().conversion(ColumnConversion.NUMBER2ENUM).map(ServiceCallTypeImpl.Fields.status.fieldName()).add();
            Column versionName = table.column("VERSIONNAME").varChar(NAME_LENGTH).notNull().map(ServiceCallTypeImpl.Fields.versionName.fieldName()).add();
            table.column("CURRENTSTATE").number().conversion(ColumnConversion.NUMBER2ENUM).map(ServiceCallTypeImpl.Fields.currentLifeCycleState.fieldName()).add();
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
    SCS_CPS_USAGE(ServiceCallTypeCustomPropertySetUsage.class) {
        @Override
        void describeTable(Table table) {
            table.map(ServiceCallTypeCustomPropertySetUsageImpl.class);
            Column serviceCallType = table.column("SERVICECALLTYPE").number().notNull().add();
            Column customPropertySet = table.column("CUSTOMPROPERTYSET").number().notNull().add();
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
    SCS_SERVICE_CALL(ServiceCall.class) {
        @Override
        void describeTable(Table table) {
            table.map(ServiceCallImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("PARENT").number().add();
            table.column("LASTCOMPLETEDTIME").number().conversion(ColumnConversion.NUMBER2INSTANT).map(ServiceCallImpl.Fields.lastCompletedTime.fieldName()).add();
            table.column("STATE").number().conversion(ColumnConversion.NUMBER2ENUM).map(ServiceCallImpl.Fields.state.fieldName()).add();
            table.column("ORIGIN").varChar(NAME_LENGTH).map(ServiceCallImpl.Fields.origin.fieldName()).add();
            table.column("EXTERNALREFERENCE").varChar(NAME_LENGTH).map(ServiceCallImpl.Fields.externalReference.fieldName()).add();
            Column serviceCallType = table.column("SERVICECALLTYPE").number().notNull().add();
            table.addAuditColumns();

            table.foreignKey("FK_SCS_SERVICECALL_SCT").
                    on(serviceCallType).
                    references(ServiceCallType.class).
                    map(ServiceCallImpl.Fields.type.fieldName()).
                    add();
            table.primaryKey("SCS_PK_SERVICECALL").on(idColumn).add();
        }
    };

    private final Class<?> api;

    TableSpecs(Class<?> api) {
        this.api = api;
    }

    public void addTo(DataModel component) {
        Table table = component.addTable(name(), api);
        describeTable(table);
    }


    abstract void describeTable(Table table);

    }
