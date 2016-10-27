package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycle;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointTransition;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

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
            table.primaryKey("PK_UPL_LIFE_CYCLE").on(id).add();
            table.unique("UK_UPL_LIFE_CYCLE_NAME").on(name, obsoleteTimestamp).add();
            table.foreignKey("FK_UPL_FSM")
                    .on(stateMachine)
                    .references(FiniteStateMachine.class)
                    .map(UsagePointLifeCycleImpl.Fields.STATE_MACHINE.fieldName())
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
            table.column("NAME").varChar().map(UsagePointTransitionImpl.Fields.NAME.fieldName()).add();
            table.column("LEVELBITS").number().notNull().conversion(ColumnConversion.NUMBER2LONG).map(UsagePointTransitionImpl.Fields.LEVELS.fieldName()).add();
            table.column("CHECKBITS").number().conversion(ColumnConversion.NUMBER2LONG).map(UsagePointTransitionImpl.Fields.CHECKS.fieldName()).add();
            table.column("ACTIONBITS").number().conversion(ColumnConversion.NUMBER2LONG).map(UsagePointTransitionImpl.Fields.ACTIONS.fieldName()).add();
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
    };

    abstract void addTo(DataModel dataModel);

}
