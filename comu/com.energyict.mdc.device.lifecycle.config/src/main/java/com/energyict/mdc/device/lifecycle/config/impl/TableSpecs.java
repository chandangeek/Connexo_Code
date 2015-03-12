package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

import com.elster.jupiter.fsm.FinateStateMachineService;
import com.elster.jupiter.orm.Column;
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
            table.primaryKey("PK_DLC_DEVICELIFECYCLE").on(id).add();
            table.foreignKey("FK_DLC_FSM")
                    .on(stateMachine)
                    .references(FinateStateMachineService.COMPONENT_NAME, "FSM_FINATE_STATE_MACHINE")
                    .map(DeviceLifeCycleImpl.Fields.STATE_MACHINE.fieldName())
                    .add();
        }
    };

    abstract void addTo(DataModel dataModel);

}