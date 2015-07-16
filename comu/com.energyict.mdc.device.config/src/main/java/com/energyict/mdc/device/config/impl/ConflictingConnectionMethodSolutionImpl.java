package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.ConflictingConnectionMethodSolution;
import com.energyict.mdc.device.config.DeviceConfigConflictMapping;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.exceptions.DestinationConnectionTaskIsEmpty;
import com.energyict.mdc.device.config.exceptions.OriginConnectionTaskIsEmpty;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.function.Supplier;

/**
 * Straightforward implementation of a ConflictingConnectionMethodSolution
 */
public class ConflictingConnectionMethodSolutionImpl implements ConflictingConnectionMethodSolution {

    private final Thesaurus thesaurus;

    enum Fields {
        CONFLICTINGMAPPING("conflictingMapping"),
        ACTION("action"),
        ORIGINCONNECTIONMETHOD("originConnectionMethod"),
        DESTINATIONCONNECTIONMETHOD("destinationConnectionMethod");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    private long id;
    @IsPresent
    private Reference<DeviceConfigConflictMapping> conflictingMapping = ValueReference.absent();
    @NotNull
    private DeviceConfigConflictMapping.ConflictingMappingAction action;
    private Reference<PartialConnectionTask> originConnectionMethod;
    private Reference<PartialConnectionTask> destinationConnectionMethod;

    @Inject
    public ConflictingConnectionMethodSolutionImpl(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public DeviceConfigConflictMapping.ConflictingMappingAction getConflictingMappingAction() {
        return action;
    }

    @Override
    public PartialConnectionTask getOriginPartialConnectionTask() {
        return originConnectionMethod.orElseThrow(originConnectionMethodIsEmpty());
    }

    private Supplier<? extends LocalizedException> originConnectionMethodIsEmpty() {
        return () -> new OriginConnectionTaskIsEmpty(thesaurus);
    }

    @Override
    public PartialConnectionTask getDestinationPartialConnectionTask() {
        return destinationConnectionMethod.orElseThrow(destinationConnectionMethodIsEmpty());
    }

    private Supplier<? extends LocalizedException> destinationConnectionMethodIsEmpty() {
        return () -> new DestinationConnectionTaskIsEmpty(thesaurus);
    }
}
