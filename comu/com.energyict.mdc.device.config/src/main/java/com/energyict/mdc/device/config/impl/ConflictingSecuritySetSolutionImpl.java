package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
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
public class ConflictingSecuritySetSolutionImpl implements ConflictingSecuritySetSolution {

    enum Fields {
        CONFLICTINGMAPPING("conflictingMapping"),
        ACTION("action"),
        ORIGINSECURITYSET("originSecuritySet"),
        DESTINATIONSECURITYSET("destinationSecuritySet");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }

    }

    private final Thesaurus thesaurus;

    private long id;
    @IsPresent
    private Reference<DeviceConfigConflictMapping> conflictingMapping = ValueReference.absent();
    @NotNull
    private DeviceConfigConflictMapping.ConflictingMappingAction action;
    private Reference<SecurityPropertySet> originSecurityPropertySet;
    private Reference<SecurityPropertySet> destinationSecurityPropertySet;

    @Inject
    public ConflictingSecuritySetSolutionImpl(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public DeviceConfigConflictMapping.ConflictingMappingAction getConflictingMappingAction() {
        return action;
    }

    @Override
    public SecurityPropertySet getOriginSecurityPropertySet() {
        return originSecurityPropertySet.orElseThrow(originSecurityPropertySetIsEmpty());
    }

    private Supplier<? extends LocalizedException> originSecurityPropertySetIsEmpty() {
        return () -> new OriginSecurityPropertySetIsEmpty(thesaurus);
    }

    @Override
    public SecurityPropertySet getDestinationSecurityPropertySet() {
        return destinationSecurityPropertySet.orElseThrow(destinationSecurityPropertySetIsEmpty());
    }

    private Supplier<? extends LocalizedException> destinationSecurityPropertySetIsEmpty() {
        return () -> new DestinationSecurityPropertySetIsEmpty(thesaurus);
    }
}
