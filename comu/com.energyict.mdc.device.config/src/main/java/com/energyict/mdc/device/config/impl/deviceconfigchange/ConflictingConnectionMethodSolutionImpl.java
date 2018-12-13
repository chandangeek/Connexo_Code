/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl.deviceconfigchange;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.ConflictingConnectionMethodSolution;
import com.energyict.mdc.device.config.DeviceConfigConflictMapping;
import com.energyict.mdc.device.config.PartialConnectionTask;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Straightforward implementation of a ConflictingConnectionMethodSolution
 */
@ActionTypeMapHasDestination(groups = {Save.Create.class, Save.Update.class}, getDestination = ConflictingConnectionMethodSolutionImpl.DESTINATION_CONNECTION_METHOD_FIELD_NAME)
public class ConflictingConnectionMethodSolutionImpl extends AbstractConflictSolution<PartialConnectionTask> implements ConflictingConnectionMethodSolution {

    /**
     * Created a static field for this so it can be used in the javaxValidation annotation
     */
    static final String DESTINATION_CONNECTION_METHOD_FIELD_NAME = "destinationConnectionMethod";

    public enum Fields {
        CONFLICTINGMAPPING("conflictingMapping"),
        ACTION("action"),
        ORIGINCONNECTIONMETHOD("originConnectionMethod"),
        DESTINATIONCONNECTIONMETHOD(DESTINATION_CONNECTION_METHOD_FIELD_NAME);

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }

    }

    private Reference<PartialConnectionTask> originConnectionMethod = ValueReference.absent();
    private Reference<PartialConnectionTask> destinationConnectionMethod = ValueReference.absent();

    @Inject
    public ConflictingConnectionMethodSolutionImpl(DataModel dataModel) {
        super(dataModel);
    }

    @Override
    public List<PartialConnectionTask> getMappableToDataSources() {
        return getConflictingMapping().getDestinationDeviceConfiguration().getPartialConnectionTasks().stream().filter(partialConnectionTask -> partialConnectionTask.getPluggableClass().getId() == getOriginDataSource().getPluggableClass().getId()).collect(Collectors.toList());
    }

    @Override
    Reference<PartialConnectionTask> getOriginDataSourceReference() {
        return originConnectionMethod;
    }

    @Override
    Reference<PartialConnectionTask> getDestinationDataSourceReference() {
        return destinationConnectionMethod;
    }

    public ConflictingConnectionMethodSolution initialize(DeviceConfigConflictMappingImpl deviceConfigConflictMapping, PartialConnectionTask origin) {
        setConflictingMapping(deviceConfigConflictMapping);
        this.originConnectionMethod.set(origin);
        this.destinationConnectionMethod.setNull();
        this.action = DeviceConfigConflictMapping.ConflictingMappingAction.NOT_DETERMINED_YET;
        return this;
    }
}