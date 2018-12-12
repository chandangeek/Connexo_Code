/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl.deviceconfigchange;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.config.ConflictingSolution;
import com.energyict.mdc.device.config.DeviceConfigConflictMapping;

import javax.validation.constraints.NotNull;

/**
 * Groups default behavior of a ConflictSolution
 */
public abstract class AbstractConflictSolution<S extends HasId> implements ConflictingSolution<S> {

    private final DataModel dataModel;
    @NotNull
    protected DeviceConfigConflictMapping.ConflictingMappingAction action;
    @IsPresent
    private Reference<DeviceConfigConflictMappingImpl> conflictingMapping = ValueReference.absent();

    @SuppressWarnings("unused")
    private long id;

    public AbstractConflictSolution(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    abstract Reference<S> getOriginDataSourceReference();
    abstract Reference<S> getDestinationDataSourceReference();

    public void update() {
        Save.UPDATE.save(dataModel, this);
    }

    @Override
    public DeviceConfigConflictMapping.ConflictingMappingAction getConflictingMappingAction() {
        return action;
    }

    @Override
    public S getOriginDataSource() {
        return getOriginDataSourceReference().get();
    }

    @Override
    public S getDestinationDataSource() {
        return getDestinationDataSourceReference().get();
    }

    void setConflictingMapping(DeviceConfigConflictMappingImpl conflictingMapping) {
        this.conflictingMapping.set(conflictingMapping);
    }

    @Override
    public void markSolutionAsRemove() {
        this.getDestinationDataSourceReference().setNull();
        updateSolution(DeviceConfigConflictMapping.ConflictingMappingAction.REMOVE);
    }

    @Override
    public void markSolutionAsMap(S dataSource) {
        this.getDestinationDataSourceReference().set(dataSource);
        updateSolution(DeviceConfigConflictMapping.ConflictingMappingAction.MAP);
    }

    private void updateSolution(DeviceConfigConflictMapping.ConflictingMappingAction action) {
        this.action = action;
        this.conflictingMapping.get().recalculateSolvedState(this);
    }

    protected DeviceConfigConflictMapping getConflictingMapping() {
        return conflictingMapping.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractConflictSolution<?> that = (AbstractConflictSolution<?>) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
