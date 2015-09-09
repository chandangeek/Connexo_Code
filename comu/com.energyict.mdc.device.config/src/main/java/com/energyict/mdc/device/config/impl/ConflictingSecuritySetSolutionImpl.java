package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.ConflictingSecuritySetSolution;
import com.energyict.mdc.device.config.DeviceConfigConflictMapping;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.exceptions.DestinationSecurityPropertySetIsEmpty;
import com.energyict.mdc.device.config.exceptions.OriginSecurityPropertySetIsEmpty;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.function.Supplier;

/**
 * Straightforward implementation ofa ConflictingSecuritySetSolution
 */
public class ConflictingSecuritySetSolutionImpl extends AbstractConflictSolution<SecurityPropertySet> implements ConflictingSecuritySetSolution {

    enum Fields {
        CONFLICTINGMAPPING("conflictingMapping"),
        ACTION("action"),
        ORIGINSECURITYSET("originSecurityPropertySet"),
        DESTINATIONSECURITYSET("destinationSecurityPropertySet");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    private final Thesaurus thesaurus;

    private Reference<SecurityPropertySet> originSecurityPropertySet = ValueReference.absent();
    private Reference<SecurityPropertySet> destinationSecurityPropertySet = ValueReference.absent();

    @Inject
    public ConflictingSecuritySetSolutionImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel);
        this.thesaurus = thesaurus;
    }

    @Override
    public SecurityPropertySet getOriginDataSource() {
        return originSecurityPropertySet.orElseThrow(originSecurityPropertySetIsEmpty());
    }

    @Override
    public SecurityPropertySet getDestinationDataSource() {
        return destinationSecurityPropertySet.orElseThrow(destinationSecurityPropertySetIsEmpty());
    }

    @Override
    void setMappedDataSource(SecurityPropertySet dataSource) {
        this.destinationSecurityPropertySet.set(dataSource);
    }

    public ConflictingSecuritySetSolution initialize(DeviceConfigConflictMappingImpl deviceConfigConflictMapping, SecurityPropertySet origin, SecurityPropertySet destination) {
        setConflictingMapping(deviceConfigConflictMapping);
        this.originSecurityPropertySet.set(origin);
        this.destinationSecurityPropertySet.set(destination);
        this.action = DeviceConfigConflictMapping.ConflictingMappingAction.NOT_DETERMINED_YET;
        return this;
    }

    private Supplier<? extends LocalizedException> originSecurityPropertySetIsEmpty() {
        return () -> new OriginSecurityPropertySetIsEmpty(thesaurus);
    }

    private Supplier<? extends LocalizedException> destinationSecurityPropertySetIsEmpty() {
        return () -> new DestinationSecurityPropertySetIsEmpty(thesaurus);
    }
}
