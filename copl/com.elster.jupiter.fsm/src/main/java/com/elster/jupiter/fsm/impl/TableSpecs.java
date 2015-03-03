package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.fsm.FinateStateMachine;
import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;

/**
 * Models the database tables that hold the data of the
 * entities that are managed by this bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (15:25)
 */
public enum TableSpecs {

    FSM_FINATE_STATE_MACHINE {
        @Override
        void addTo(DataModel dataModel) {
            Table<FinateStateMachine> table = dataModel.addTable(this.name(), FinateStateMachine.class);
            table.map(FinateStateMachineImpl.IMPLEMENTERS);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            table.addDiscriminatorColumn("DISCRIMINATOR", "char(1)");
            Column name = table.column("NAME").varChar().notNull().map(FinateStateMachineImpl.Fields.NAME.fieldName()).add();
            table.unique("UK_FSM_FINATESTATEMACHINE").on(name).add();
            table.primaryKey("PK_FSM_FINATESTATEMACHINE").on(id).add();
        }
    },

    FSM_EVENT_TYPE {
        @Override
        void addTo(DataModel dataModel) {
            Table<StateTransitionEventType> table = dataModel.addTable(this.name(), StateTransitionEventType.class);
            table.map(StateTransitionEventTypeImpl.class);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            Column name = table.column("SYMBOL").varChar().notNull().map(StateTransitionEventTypeImpl.Fields.SYMBOL.fieldName()).add();
            table.unique("UK_FSM_EVENTTYPE").on(name).add();
            table.primaryKey("PK_FSM_EVENTTYPE").on(id).add();
        }
    },

    FSM_STATE {
        @Override
        void addTo(DataModel dataModel) {
            Table<State> table = dataModel.addTable(this.name(), State.class);
            table.map(StateImpl.IMPLEMENTERS);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            table.addDiscriminatorColumn("DISCRIMINATOR", "char(1)");
            Column name = table.column("NAME").varChar().notNull().map(StateImpl.Fields.NAME.fieldName()).add();
            Column finateStateMachine = table.column("FSM").number().notNull().add();
            table.unique("UK_FSM_STATE").on(name).add();
            table.foreignKey("FK_FSM_STATE_FSM")
                    .on(finateStateMachine)
                    .references(FSM_FINATE_STATE_MACHINE.name())
                    .map(StateImpl.Fields.FINATE_STATE_MACHINE.fieldName())
                    .reverseMap(FinateStateMachineImpl.Fields.STATES.fieldName())
                    .composition()
                    .add();
            table.primaryKey("PK_FSM_STATE").on(id).add();
        }
    },

    FSM_PROCESS {
        @Override
        void addTo(DataModel dataModel) {
            Table<ProcessReference> table = dataModel.addTable(this.name(), ProcessReference.class);
            table.map(ProcessReferenceImpl.class);
            Column id = table.addAutoIdColumn();
            table.column("PURPOSE").number().notNull().conversion(ColumnConversion.NUMBER2ENUM).map(ProcessReferenceImpl.Fields.PURPOSE.fieldName()).add();
            table.column("DEPLOYMENTID").varChar().notNull().map(ProcessReferenceImpl.Fields.DEPLOYMENT_ID.fieldName()).add();
            table.column("PROCESSID").varChar().notNull().map(ProcessReferenceImpl.Fields.PROCESS_ID.fieldName()).add();
            Column state = table.column("STATE").number().notNull().add();
            table.primaryKey("PK_FSM_PROCESS").on(id).add();
            table.foreignKey("FK_FSM_PROCESS_STATE")
                    .on(state)
                    .references(FSM_STATE.name())
                    .map(ProcessReferenceImpl.Fields.STATE.fieldName())
                    .reverseMap(StateImpl.Fields.PROCESS_REFERENCES.fieldName())
                    .composition()
                    .add();
        }
    },

    FSM_STATE_TRANSITION {
        @Override
        void addTo(DataModel dataModel) {
            Table<StateTransition> table = dataModel.addTable(this.name(), StateTransition.class);
            table.map(StateTransitionImpl.class);
            Column id = table.addAutoIdColumn();
            Column from = table.column("FROMSTATE").number().notNull().add();
            Column to = table.column("TOSTATE").number().notNull().map(StateTransitionImpl.Fields.TO.fieldName()).add();
            Column eventType = table.column("EVENTTYPE").number().notNull().map(StateTransitionImpl.Fields.EVENT_TYPE.fieldName()).add();
            Column finateStateMachine = table.column("FSM").number().notNull().add();
            table.primaryKey("PK_FSM_STATE_TRANSITION").on(id).add();
            table.foreignKey("FK_FSM_STATETRANS_FSM")
                    .on(finateStateMachine)
                    .references(FSM_FINATE_STATE_MACHINE.name())
                    .map(StateTransitionImpl.Fields.FINATE_STATE_MACHINE.fieldName())
                    .reverseMap(FinateStateMachineImpl.Fields.TRANSITIONS.fieldName())
                    .composition()
                    .add();
            table.foreignKey("FK_FSM_STATETRANS_STATE_FROM")
                    .on(from)
                    .references(FSM_STATE.name())
                    .map(StateTransitionImpl.Fields.FROM.fieldName())
                    .add();
            table.foreignKey("FK_FSM_STATETRANS_STATE_TO")
                    .on(to)
                    .references(FSM_STATE.name())
                    .map(StateTransitionImpl.Fields.TO.fieldName())
                    .add();
            table.foreignKey("FK_FSM_STATETRANS_EVENT_TYPE")
                    .on(eventType)
                    .references(FSM_EVENT_TYPE.name())
                    .map(StateTransitionImpl.Fields.EVENT_TYPE.fieldName())
                    .add();
        }
    };

    abstract void addTo(DataModel dataModel);

}