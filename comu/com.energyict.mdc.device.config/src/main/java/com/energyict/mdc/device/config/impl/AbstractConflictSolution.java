package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.common.HasId;
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

    /**
     * Sets the dataSource to map when the action is set to {@link DeviceConfigConflictMapping.ConflictingMappingAction#NOT_DETERMINED_YET}
     *
     * @param dataSource the DataSource to map
     */
    abstract void setMappedDataSource(S dataSource);

    public void update() {
        Save.UPDATE.save(dataModel, this);
    }

    @Override
    public DeviceConfigConflictMapping.ConflictingMappingAction getConflictingMappingAction() {
        return action;
    }

    public void setConflictingMapping(DeviceConfigConflictMappingImpl conflictingMapping) {
        this.conflictingMapping.set(conflictingMapping);
    }

    @Override
    public void setSolution(DeviceConfigConflictMapping.ConflictingMappingAction action) {
        this.action = action;
        this.conflictingMapping.get().recalculateSolvedState(this);
    }

    @Override
    public void setSolution(DeviceConfigConflictMapping.ConflictingMappingAction action, S dataSource) {
        setMappedDataSource(dataSource);
        setSolution(action);
    }
}
