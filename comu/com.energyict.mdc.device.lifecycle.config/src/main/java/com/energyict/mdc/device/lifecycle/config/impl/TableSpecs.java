package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

import com.elster.jupiter.fsm.FinateStateMachineService;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

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
            Column stateMachine = table.column("FSM").number().notNull().add();
            table.primaryKey("PK_DLD_DEVICELIFECYCLE").on(id).add();
            table.foreignKey("FK_DLD_FSM")
                    .on(stateMachine)
                    .references(FinateStateMachineService.COMPONENT_NAME, "FSM_FINATE_STATE_MACHINE")
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
            table.column("TRANSITIONTYPE").number().conversion(ColumnConversion.NUMBER2ENUM).map(AuthorizedActionImpl.Fields.TYPE.fieldName()).add();
            // AuthorizedBusinessProcessAction
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
        }
    };

    abstract void addTo(DataModel dataModel);

}