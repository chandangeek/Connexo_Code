package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.ConflictingConnectionMethodSolution;
import com.energyict.mdc.device.config.DeviceConfigConflictMapping;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.exceptions.DestinationConnectionTaskIsEmpty;
import com.energyict.mdc.device.config.exceptions.OriginConnectionTaskIsEmpty;

import javax.inject.Inject;
import java.util.function.Supplier;

/**
 * Straightforward implementation of a ConflictingConnectionMethodSolution
 */
public class ConflictingConnectionMethodSolutionImpl extends AbstractConflictSolution<PartialConnectionTask> implements ConflictingConnectionMethodSolution {


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

    private Thesaurus thesaurus;

    private Reference<PartialConnectionTask> originConnectionMethod = ValueReference.absent();
    private Reference<PartialConnectionTask> destinationConnectionMethod = ValueReference.absent();

    @Inject
    public ConflictingConnectionMethodSolutionImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel);
        this.thesaurus = thesaurus;
    }

    @Override
    public PartialConnectionTask getOriginDataSource() {
        return originConnectionMethod.orElseThrow(originConnectionMethodIsEmpty());
    }

    @Override
    public PartialConnectionTask getDestinationDataSource() {
        return destinationConnectionMethod.orElseThrow(destinationConnectionMethodIsEmpty());
    }

    public ConflictingConnectionMethodSolutionImpl initialize(DeviceConfigConflictMappingImpl deviceConfigConflictMapping, PartialConnectionTask origin, PartialConnectionTask destination) {
        setConflictingMapping(deviceConfigConflictMapping);
        this.originConnectionMethod.set(origin);
        this.destinationConnectionMethod.set(destination);
        this.action = DeviceConfigConflictMapping.ConflictingMappingAction.NOT_DETERMINED_YET;
        return this;
    }

    private Supplier<? extends LocalizedException> originConnectionMethodIsEmpty() {
        return () -> new OriginConnectionTaskIsEmpty(thesaurus);
    }

    private Supplier<? extends LocalizedException> destinationConnectionMethodIsEmpty() {
        return () -> new DestinationConnectionTaskIsEmpty(thesaurus);
    }
}