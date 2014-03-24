package com.energyict.mdc.tasks.task.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.RegisterGroup;
import com.energyict.mdc.tasks.task.RegistersTask;

/**
 * Link table from RegisterTask to RegisterGroup
 */
public class RegisterGroupUsageImpl implements RegisterGroupUsage {

    enum Fields {
        REGISTERS_TASK_REFERENCE("registersTaskReference"),
        REGISTERS_GROUP_REFERENCE("registersGroupReference");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    private Reference<RegistersTask> registersTaskReference = ValueReference.absent();
    private Reference<RegisterGroup> registersGroupReference = ValueReference.absent();

    @Override
    public RegistersTask getRegistersTask() {
        return registersTaskReference.get();
    }

    @Override
    public void setRegistersTask(RegistersTask registersTaskReference) {
        this.registersTaskReference.set(registersTaskReference);
    }

    @Override
    public RegisterGroup getRegistersGroup() {
        return registersGroupReference.get();
    }

    @Override
    public void setRegistersGroup(RegisterGroup registersGroupReference) {
        this.registersGroupReference.set(registersGroupReference);
    }
}
