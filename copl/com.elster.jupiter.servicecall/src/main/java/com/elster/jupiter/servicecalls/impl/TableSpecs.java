package com.elster.jupiter.servicecalls.impl;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.servicecalls.ServiceCallLifeCycle;

import static com.elster.jupiter.orm.DeleteRule.CASCADE;
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
