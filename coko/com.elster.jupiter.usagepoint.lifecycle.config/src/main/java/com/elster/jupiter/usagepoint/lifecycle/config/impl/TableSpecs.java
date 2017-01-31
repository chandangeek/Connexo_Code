/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;

public enum TableSpecs {
    UPL_LIFE_CYCLE {
        @Override
        void addTo(DataModel dataModel) {
            Table<UsagePointLifeCycle> table = dataModel.addTable(this.name(), UsagePointLifeCycle.class);
            table.map(UsagePointLifeCycleImpl.class);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            table.setJournalTableName(name() + "JRNL");
            Column name = table.column("NAME").varChar().notNull().map(UsagePointLifeCycleImpl.Fields.NAME.fieldName()).add();
            Column obsoleteTimestamp = table.column("OBSOLETE_TIME").number().conversion(ColumnConversion.NUMBER2INSTANT).map(UsagePointLifeCycleImpl.Fields.OBSOLETE_TIME.fieldName()).add();
            Column stateMachine = table.column("FSM").number().notNull().add();
            table.column("IS_DEFAULT").bool().map(UsagePointLifeCycleImpl.Fields.DEFAULT.fieldName()).add();
            table.primaryKey("PK_UPL_LIFE_CYCLE").on(id).add();
            table.unique("UK_UPL_LIFE_CYCLE_NAME").on(name, obsoleteTimestamp).add();
            table.foreignKey("FK_UPL_FSM")
                    .on(stateMachine)
                    .references(FiniteStateMachine.class)
                    .map(UsagePointLifeCycleImpl.Fields.STATE_MACHINE.fieldName())
                    .add();
        }
    },
    UPL_STATE {
        @Override
        void addTo(DataModel dataModel) {
            Table<UsagePointState> table = dataModel.addTable(this.name(), UsagePointState.class);
            table.map(UsagePointStateImpl.class);

            Column idColumn = table.column("ID").map("id").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            Column stateColumn = table.column("STATE").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            Column lifeCycleColumn = table.column("LIFE_CYCLE").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            table.column("STAGE").map(UsagePointStateImpl.Fields.STAGE.fieldName()).varChar(Table.NAME_LENGTH).conversion(ColumnConversion.CHAR2ENUM).notNull().add();
            table.addAuditColumns();

            table.primaryKey("PK_UPL_STATE").on(idColumn).add();
            table.foreignKey("FK_UPL_STATE_2_LIFE_CYCLE")
                    .on(lifeCycleColumn)
                    .references(UPL_LIFE_CYCLE.name())
                    .map(UsagePointStateImpl.Fields.LIFE_CYCLE.fieldName())
                    .reverseMap(UsagePointLifeCycleImpl.Fields.STATES.fieldName())
                    .composition()
                    .add();
            table.foreignKey("FK_UPL_2_FSM_STATE")
                    .on(stateColumn)
                    .references(State.class)
                    .map(UsagePointStateImpl.Fields.STATE.fieldName())
                    .add();
        }
    },
    UPL_TRANSITION {
        @Override
        void addTo(DataModel dataModel) {
            Table<UsagePointTransition> table = dataModel.addTable(this.name(), UsagePointTransition.class);
            table.map(UsagePointTransitionImpl.class);
            table.setJournalTableName(name() + "JRNL");
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            Column lifeCycle = table.column("LIFE_CYCLE").number().notNull().add();
            table.column("NAME").varChar().map(UsagePointTransitionImpl.Fields.NAME.fieldName()).notNull().add();
            table.column("LEVELBITS").number().notNull().conversion(ColumnConversion.NUMBER2LONG).map(UsagePointTransitionImpl.Fields.LEVELS.fieldName()).add();
            Column fsmTransition = table.column("FSM_TRANSITION").number().add();

            table.primaryKey("PK_UPL_TRANSITION").on(id).add();
            table.foreignKey("FK_UPL_TRANSITION_2_LIFE_CYCLE")
                    .on(lifeCycle)
                    .references(UPL_LIFE_CYCLE.name())
                    .map(UsagePointTransitionImpl.Fields.LIFE_CYCLE.fieldName())
                    .reverseMap(UsagePointLifeCycleImpl.Fields.TRANSITIONS.fieldName())
                    .composition()
                    .add();
            table.foreignKey("FK_UPL_2_FSM_TRANSITION")
                    .on(fsmTransition)
                    .references(StateTransition.class)
                    .map(UsagePointTransitionImpl.Fields.FSM_TRANSITION.fieldName())
                    .add();
        }
    },

    UPL_TRANSITION_CHECKS {
        @Override
        void addTo(DataModel dataModel) {
            Table<UsagePointTransitionMicroCheckUsageImpl> table = dataModel.addTable(this.name(), UsagePointTransitionMicroCheckUsageImpl.class);
            table.map(UsagePointTransitionMicroCheckUsageImpl.class);
            table.setJournalTableName(name() + "JRNL");
            Column transition = table.column("TRANSITION").number().notNull().add();
            Column check = table.column("MICRO_CHECK").varChar().map(UsagePointTransitionMicroCheckUsageImpl.Fields.MICRO_CHECK.fieldName()).notNull().add();

            table.primaryKey("PK_UPL_TRANSITION_CHECKS").on(transition, check).add();
            table.foreignKey("FK_UPL_CHECK_2_TRANS")
                    .on(transition)
                    .references(UsagePointTransition.class)
                    .map(UsagePointTransitionMicroCheckUsageImpl.Fields.TRANSITION.fieldName())
                    .reverseMap(UsagePointTransitionImpl.Fields.CHECKS.fieldName())
                    .composition()
                    .add();
        }
    },

    UPL_TRANSITION_ACTION {
        @Override
        void addTo(DataModel dataModel) {
            Table<UsagePointTransitionMicroActionUsageImpl> table = dataModel.addTable(this.name(), UsagePointTransitionMicroActionUsageImpl.class);
            table.map(UsagePointTransitionMicroActionUsageImpl.class);
            table.setJournalTableName(name() + "JRNL");
            Column transition = table.column("TRANSITION").number().notNull().add();
            Column action = table.column("MICRO_ACTION").varChar().map(UsagePointTransitionMicroActionUsageImpl.Fields.MICRO_ACTION.fieldName()).notNull().add();

            table.primaryKey("PK_UPL_TRANSITION_ACTIONS").on(transition, action).add();
            table.foreignKey("FK_UPL_ACTION_2_TRANS")
                    .on(transition)
                    .references(UsagePointTransition.class)
                    .map(UsagePointTransitionMicroActionUsageImpl.Fields.TRANSITION.fieldName())
                    .reverseMap(UsagePointTransitionImpl.Fields.ACTIONS.fieldName())
                    .composition()
                    .add();
        }
    };

    abstract void addTo(DataModel dataModel);

}
