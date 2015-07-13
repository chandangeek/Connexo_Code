package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.ConflictingConnectionMethodSolution;
import com.energyict.mdc.device.config.DeviceConfigConflictMapping;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;

import javax.validation.constraints.NotNull;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 13/07/15
 * Time: 15:14
 */
public class ConflictingConnectionMethodSolutionImpl implements ConflictingConnectionMethodSolution {

    enum Fields {
        CONFLICTINGMAPPING("conflictingMapping"),
        ACTION("action"),
        ORIGINCONNECTIONMETHOD("originConnectionMethod"),
        DESTINATIONCONNECTIONMETHOD("destinationConnectionMethod")
        ;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent
    private final Reference<DeviceConfigConflictMapping> conflictingMapping = ValueReference.absent();
    @NotNull
    private DeviceConfigConflictMapping.ConflictingMappingAction action;
    private Reference<PartialConnectionTask> originConnectionMethod;
    private Reference<PartialConnectionTask> destinationConnectionMethod;


    @Override
    public DeviceConfigConflictMapping.ConflictingMappingAction getConflictingMappingAction() {
        return action;
    }

    @Override
    public PartialConnectionTask getOriginPartialConnectionTask() {
        return null;
    }

    @Override
    public PartialConnectionTask getDestinationPartialConnectionTask() {
        return null;
    }

}
