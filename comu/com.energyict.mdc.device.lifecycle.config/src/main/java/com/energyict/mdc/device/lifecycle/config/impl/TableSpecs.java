package com.energyict.mdc.device.lifecycle.config.impl;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.TransitionBusinessProcess;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
import static com.elster.jupiter.orm.Version.version;

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
            table.setJournalTableName("DLD_DEVICE_LIFE_CYCLEJRNL").since(version(10, 2));
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            Column name = table.column("NAME").varChar().notNull().map(DeviceLifeCycleImpl.Fields.NAME.fieldName()).add();
            Column obsoleteTimestamp = table.column("OBSOLETE_TIMESTAMP").number().conversion(ColumnConversion.NUMBER2INSTANT).map(DeviceLifeCycleImpl.Fields.OBSOLETE_TIMESTAMP.fieldName()).add();
            Column stateMachine = table.column("FSM").number().notNull().add();
            table.column("MAXFUTUREEFFTIMESHIFTVALUE").number().notNull().conversion(NUMBER2INT).map(DeviceLifeCycleImpl.Fields.MAX_FUTURE_EFFECTIVE_TIME_SHIFT.fieldName() + ".count").add();
            table.column("MAXFUTUREEFFTIMESHIFTUNIT").number().notNull().conversion(NUMBER2INT).map(DeviceLifeCycleImpl.Fields.MAX_FUTURE_EFFECTIVE_TIME_SHIFT.fieldName() + ".timeUnitCode").add();
            table.column("MAXPASTEFFTIMESHIFTVALUE").number().notNull().conversion(NUMBER2INT).map(DeviceLifeCycleImpl.Fields.MAX_PAST_EFFECTIVE_TIME_SHIFT.fieldName() + ".count").add();
            table.column("MAXPASTEFFTIMESHIFTUNIT").number().notNull().conversion(NUMBER2INT).map(DeviceLifeCycleImpl.Fields.MAX_PAST_EFFECTIVE_TIME_SHIFT.fieldName() + ".timeUnitCode").add();
            table.primaryKey("PK_DLD_DEVICELIFECYCLE").on(id).add();
            table.unique("UK_DLD_DEVICELIFECYCLENAME").on(name, obsoleteTimestamp).add();
            table.foreignKey("FK_DLD_FSM")
                    .on(stateMachine)
                    .references(FiniteStateMachine.class)
                    .map(DeviceLifeCycleImpl.Fields.STATE_MACHINE.fieldName())
                    .add();
        }
    },
    DLD_TRANSITION_PROCESS {
        @Override
        void addTo(DataModel dataModel) {
            Table<TransitionBusinessProcess> table = dataModel.addTable(this.name(), TransitionBusinessProcess.class);
            table.map(TransitionBusinessProcessImpl.class);
            Column id = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar().notNull().map(TransitionBusinessProcessImpl.Fields.NAME.fieldName()).add();
            table.column("DEPLOYMENTID").varChar().notNull().map(TransitionBusinessProcessImpl.Fields.DEPLOYMENT_ID.fieldName()).add();
            table.column("PROCESSID").varChar().notNull().map(TransitionBusinessProcessImpl.Fields.PROCESS_ID.fieldName()).add();
            table.primaryKey("PK_TRANSITION_PROCESS").on(id).add();
            table.unique("UK_DLD_TRANSITIONPROCESSNAME").on(name).add();
        }
    },

    DLD_AUTHORIZED_ACTION {
        @Override
        void addTo(DataModel dataModel) {
            Table<AuthorizedAction> table = dataModel.addTable(this.name(), AuthorizedAction.class);
            table.map(AuthorizedActionImpl.IMPLEMENTERS);
            table.setJournalTableName("DLD_AUTHORIZED_ACTIONJRNL").since(version(10, 2));
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
            Column process = table.column("PROCESS").number().add();
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
                    .references(State.class)
                    .map(AuthorizedActionImpl.Fields.STATE.fieldName())
                    .add();
            table.foreignKey("FK_DLD_AUTH_ACTION_STATETRANS")
                    .on(stateTransition)
                    .references(StateTransition.class)
                    .map(AuthorizedActionImpl.Fields.STATE_TRANSITION.fieldName())
                    .add();
            table.foreignKey("FK_DLD_AUTH_ACTION_PROCESS")
                    .on(process)
                    .references(DLD_TRANSITION_PROCESS.name())
                    .map(AuthorizedActionImpl.Fields.PROCESS.fieldName())
                    .add();
        }
    };

    abstract void addTo(DataModel dataModel);

}