package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;

/**
 * Models the database tables that hold the data of the
 * entities that are managed by this bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (10:25)
 */
public enum TableSpecs {
    DLD_DEVICE_LIFE_CYCLE {
        @Override
        void addTo(DataModel dataModel) {
            Table<DeviceLifeCycle> table = dataModel.addTable(this.name(), DeviceLifeCycle.class);
            table.map(DeviceLifeCycleImpl.class);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            Column name = table.column("NAME").varChar().notNull().map(DeviceLifeCycleImpl.Fields.NAME.fieldName()).add();
            Column stateMachine = table.column("FSM").number().notNull().add();
            table.column("MAXFUTUREEFFTIMESHIFTVALUE").number().notNull().conversion(NUMBER2INT).map(DeviceLifeCycleImpl.Fields.MAX_FUTURE_EFFECTIVE_TIME_SHIFT.fieldName() + ".count").add();
            table.column("MAXFUTUREEFFTIMESHIFTUNIT").number().notNull().conversion(NUMBER2INT).map(DeviceLifeCycleImpl.Fields.MAX_FUTURE_EFFECTIVE_TIME_SHIFT.fieldName() + ".timeUnitCode").add();
            table.column("MAXPASTEFFTIMESHIFTVALUE").number().notNull().conversion(NUMBER2INT).map(DeviceLifeCycleImpl.Fields.MAX_PAST_EFFECTIVE_TIME_SHIFT.fieldName() + ".count").add();
            table.column("MAXPASTEFFTIMESHIFTUNIT").number().notNull().conversion(NUMBER2INT).map(DeviceLifeCycleImpl.Fields.MAX_PAST_EFFECTIVE_TIME_SHIFT.fieldName() + ".timeUnitCode").add();
            table.unique("UK_DLD_DEVICELIFECYCLENAME").on(name).add();
            table.primaryKey("PK_DLD_DEVICELIFECYCLE").on(id).add();
            table.foreignKey("FK_DLD_FSM")
                    .on(stateMachine)
                    .references(FiniteStateMachineService.COMPONENT_NAME, "FSM_FINITE_STATE_MACHINE")
                    .map(DeviceLifeCycleImpl.Fields.STATE_MACHINE.fieldName())
                    .add();
        }
    },
    DLD_AUTHORIZED_ACTION {
        @Override
        void addTo(DataModel dataModel) {
            Table<AuthorizedAction> table = dataModel.addTable(this.name(), AuthorizedAction.class);
            table.map(AuthorizedActionImpl.IMPLEMENTERS);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            table.addDiscriminatorColumn("DISCRIMINATOR", "char(1)");
            Column deviceLifeCycle = table.column("DEVICELIFECYCLE").number().notNull().add();
            table.column("LEVELBITS").number().notNull().conversion(ColumnConversion.NUMBER2INT).map(AuthorizedActionImpl.Fields.LEVELS.fieldName()).add();
            // AuthorizedTransitionAction
            table.column("CHECKBITS").number().conversion(ColumnConversion.NUMBER2LONG).map(AuthorizedActionImpl.Fields.CHECKS.fieldName()).add();
            table.column("ACTIONBITS").number().conversion(ColumnConversion.NUMBER2LONG).map(AuthorizedActionImpl.Fields.ACTIONS.fieldName()).add();
            // AuthorizedStandardTransitionAction
            Column stateTransition = table.column("STATETRANSITION").number().add();
            table.column("TRANSITIONTYPE").number().conversion(ColumnConversion.NUMBER2ENUM).map(AuthorizedActionImpl.Fields.TYPE.fieldName()).add();
            // AuthorizedBusinessProcessAction
            Column state = table.column("STATE").number().add();
            table.column("NAME").varChar().map(AuthorizedActionImpl.Fields.NAME.fieldName()).add();
            table.column("DEPLOYMENTID").varChar().map(AuthorizedActionImpl.Fields.DEPLOYMENT_ID.fieldName()).add();
            table.column("PROCESSID").varChar().map(AuthorizedActionImpl.Fields.PROCESS_ID.fieldName()).add();
            table.primaryKey("PK_DLD_AUTHORIZED_ACTION").on(id).add();
            table.foreignKey("FK_DLD_AUTH_ACTION_DLC")
                    .on(deviceLifeCycle)
                    .references(DLD_DEVICE_LIFE_CYCLE.name())
                    .map(AuthorizedActionImpl.Fields.DEVICE_LIFE_CYCLE.fieldName())
                    .reverseMap(DeviceLifeCycleImpl.Fields.ACTIONS.fieldName())
                    .composition()
                    .add();
            table.foreignKey("FK_DLD_AUTH_ACTION_STATE")
                    .on(state)
                    .references(FiniteStateMachineService.COMPONENT_NAME, "FSM_STATE")
                    .map(AuthorizedActionImpl.Fields.STATE.fieldName())
                    .add();
            table.foreignKey("FK_DLD_AUTH_ACTION_STATETRANS")
                    .on(stateTransition)
                    .references(FiniteStateMachineService.COMPONENT_NAME, "FSM_STATE_TRANSITION")
                    .map(AuthorizedActionImpl.Fields.STATE_TRANSITION.fieldName())
                    .add();
        }
    };

    abstract void addTo(DataModel dataModel);

}