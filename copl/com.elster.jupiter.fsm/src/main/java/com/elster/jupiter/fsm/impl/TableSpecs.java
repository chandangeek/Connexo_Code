package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateChangeBusinessProcess;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.Table.NAME_LENGTH;

/**
 * Models the database tables that hold the data of the
 * entities that are managed by this bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (15:25)
 */
public enum TableSpecs {

    FSM_FINITE_STATE_MACHINE {
        @Override
        void addTo(DataModel dataModel) {
            Table<FiniteStateMachine> table = dataModel.addTable(this.name(), FiniteStateMachine.class);
            table.map(FiniteStateMachineImpl.class);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            Column name = table.column("NAME").varChar().notNull().map(FiniteStateMachineImpl.Fields.NAME.fieldName()).add();
            Column obsoleteTimestamp = table.column("OBSOLETE_TIMESTAMP").number().conversion(ColumnConversion.NUMBER2INSTANT).map(FiniteStateMachineImpl.Fields.OBSOLETE_TIMESTAMP.fieldName()).add();
            table.primaryKey("PK_FSM_FINITESTATEMACHINE").on(id).add();
            table.unique("UK_FSM_FINITESTATEMACHINE").on(name, obsoleteTimestamp).add();
        }
    },

    FSM_EVENT_TYPE {
        @Override
        void addTo(DataModel dataModel) {
            Table<StateTransitionEventType> table = dataModel.addTable(this.name(), StateTransitionEventType.class);
            table.map(StateTransitionEventTypeImpl.IMPLEMENTERS);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            table.addDiscriminatorColumn("DISCRIMINATOR", "char(1)");
            Column symbol = table.column("SYMBOL").varChar().map(StateTransitionEventTypeImpl.Fields.SYMBOL.fieldName()).add();
            Column eventType = table.column("EVENTTYPE").varChar(NAME_LENGTH).add();
            table.unique("UK_FSM_EVENTTYPE_SYMBOL").on(symbol).add();
            table.unique("UK_FSM_EVENTTYPE").on(eventType).add();
            table.primaryKey("PK_FSM_EVENTTYPE").on(id).add();
            table.foreignKey("FK_FSM_EVENTTYPE")
                .on(eventType)
                .references(EventService.COMPONENTNAME, "EVT_EVENTTYPE")
                .map(StateTransitionEventTypeImpl.Fields.EVENT_TYPE.fieldName())
                .add();
        }
    },

    FSM_STATE {
        @Override
        void addTo(DataModel dataModel) {
            Table<State> table = dataModel.addTable(this.name(), State.class);
            table.map(StateImpl.class);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            Column name = table.column("NAME").varChar().notNull().map(StateImpl.Fields.NAME.fieldName()).add();
            Column obsolete = table.column("OBSOLETE_TIMESTAMP").number().conversion(ColumnConversion.NUMBER2INSTANT).map(StateImpl.Fields.OBSOLETE_TIMESTAMP.fieldName()).add();
            table.column("ISINITIAL").number().notNull().conversion(ColumnConversion.NUMBER2BOOLEAN).map(StateImpl.Fields.INITIAL.fieldName()).add();
            table.column("CUSTOM").number().notNull().conversion(ColumnConversion.NUMBER2BOOLEAN).map(StateImpl.Fields.CUSTOM.fieldName()).add();
            Column finiteStateMachine = table.column("FSM").number().notNull().add();
            table.unique("UK_FSM_STATE").on(finiteStateMachine, name, obsolete).add();
            table.foreignKey("FK_FSM_STATE_FSM")
                    .on(finiteStateMachine)
                    .references(FSM_FINITE_STATE_MACHINE.name())
                    .map(StateImpl.Fields.FINITE_STATE_MACHINE.fieldName())
                    .reverseMap(FiniteStateMachineImpl.Fields.STATES.fieldName())
                    .composition()
                    .add();
            table.primaryKey("PK_FSM_STATE").on(id).add();
        }
    },

    FSM_STATE_CHANGE_PROCESS {
        @Override
        void addTo(DataModel dataModel) {
            Table<StateChangeBusinessProcess> table = dataModel.addTable(this.name(), StateChangeBusinessProcess.class);
            table.map(StateChangeBusinessProcessImpl.class);
            Column id = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar().notNull().map(StateChangeBusinessProcessImpl.Fields.NAME.fieldName()).add();
            table.column("DEPLOYMENTID").varChar().notNull().map(StateChangeBusinessProcessImpl.Fields.DEPLOYMENT_ID.fieldName()).add();
            table.column("PROCESSID").varChar().notNull().map(StateChangeBusinessProcessImpl.Fields.PROCESS_ID.fieldName()).add();
            table.primaryKey("PK_STATE_CHANGE_PROCESS").on(id).add();
            table.unique("UK_FSM_STATE_CHANGE_NAME").on(name).add();
        }
    },

    FSM_PROCESS {
        @Override
        void addTo(DataModel dataModel) {
            Table<ProcessReference> table = dataModel.addTable(this.name(), ProcessReference.class);
            table.map(ProcessReferenceImpl.class);
            Column id = table.addAutoIdColumn();
            table.column("PURPOSE").number().notNull().conversion(ColumnConversion.NUMBER2ENUM).map(ProcessReferenceImpl.Fields.PURPOSE.fieldName()).add();
            Column process = table.column("PROCESS").number().notNull().add();
            Column state = table.column("STATE").number().notNull().add();
            table.primaryKey("PK_FSM_PROCESS").on(id).add();
            table.foreignKey("FK_FSM_PROCESS")
                    .on(process)
                    .references(FSM_STATE_CHANGE_PROCESS.name())
                    .map(ProcessReferenceImpl.Fields.PROCESS.fieldName())
                    .add();
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
            Column to = table.column("TOSTATE").number().notNull().add();
            table.column("NAME").varChar().map(StateTransitionImpl.Fields.NAME.fieldName()).add();
            table.column("NAMEKEY").varChar().map(StateTransitionImpl.Fields.NAME_KEY.fieldName()).add();
            Column eventType = table.column("EVENTTYPE").number().notNull().add();
            Column finiteStateMachine = table.column("FSM").number().notNull().add();
            table.primaryKey("PK_FSM_STATE_TRANSITION").on(id).add();
            table.foreignKey("FK_FSM_STATETRANS_FSM")
                    .on(finiteStateMachine)
                    .references(FSM_FINITE_STATE_MACHINE.name())
                    .map(StateTransitionImpl.Fields.FINITE_STATE_MACHINE.fieldName())
                    .reverseMap(FiniteStateMachineImpl.Fields.TRANSITIONS.fieldName())
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
            table.unique("UK_FSM_FROM_TO_EVENT_TYPE")
                    .on(from, eventType)
                    .add();
        }
    };

    abstract void addTo(DataModel dataModel);

}