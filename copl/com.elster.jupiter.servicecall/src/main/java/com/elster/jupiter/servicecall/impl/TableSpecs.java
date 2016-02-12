package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.servicecall.ServiceCallLifeCycle;
import com.elster.jupiter.servicecall.ServiceCallType;

import static com.elster.jupiter.orm.Table.NAME_LENGTH;

/**
 * Created by bvn on 2/4/16.
 */
public enum TableSpecs {
    SERVICE_CALL_LIFECYCLE(ServiceCallLifeCycle.class) {
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
            table.primaryKey("SCS_PK_SERVICECALL").on(idColumn).add();
        }
    },

    SERVICE_CALL_TYPE(ServiceCallType.class) {
        @Override
        void describeTable(Table table) {
            table.map(ServiceCallTypeImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").varChar(NAME_LENGTH).notNull().map(ServiceCallTypeImpl.Fields.name.fieldName()).add();
            table.column("LOGLEVEL").number().conversion(ColumnConversion.NUMBER2ENUM).map(ServiceCallTypeImpl.Fields.logLevel.fieldName()).add();
            table.column("STATUS").number().conversion(ColumnConversion.NUMBER2ENUM).map(ServiceCallTypeImpl.Fields.status.fieldName()).add();
            table.column("VERSIONNAME").varChar(NAME_LENGTH).notNull().map(ServiceCallTypeImpl.Fields.versionName.fieldName()).add();
            table.column("CURRENTSTATE").number().conversion(ColumnConversion.NUMBER2ENUM).map(ServiceCallTypeImpl.Fields.currentLifeCycleState.fieldName()).add();
            Column serviceCallLifeCycle = table.column("LIFECYCLE").number().notNull().add();
            table.addAuditColumns();
            table.foreignKey("FK_LIFECYCLE")
                    .references(ServiceCallLifeCycle.class)
                    .map(ServiceCallTypeImpl.Fields.serviceCallLifeCycle.fieldName())
                    .on(serviceCallLifeCycle)
                    .add();
            table.primaryKey("SCT_PK_SERVICECALLTYPE").on(idColumn).add();

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
