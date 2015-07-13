package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.ConflictingSecuritySetSolution;
import com.energyict.mdc.device.config.DeviceConfigConflictMapping;
import com.energyict.mdc.device.config.SecurityPropertySet;

/**
 * Copyrights EnergyICT
 * Date: 13/07/15
 * Time: 15:16
 */
public class ConflictingSecuritySetSolutionImpl implements ConflictingSecuritySetSolution {

    enum Fields {
        CONFLICTINGMAPPING("conflictingMapping"),
        ACTION("action"),
        ORIGINSECURITYSET("originSecuritySet"),
        DESTINATIONSECURITYSET("destinationSecuritySet")
        ;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    @Override
    public DeviceConfigConflictMapping.ConflictingMappingAction getConflictingMappingAction() {
        return null;
    }

    @Override
    public SecurityPropertySet getOriginSecurityPropertySet() {
        return null;
    }

    @Override
    public SecurityPropertySet getDestinationSecurityPropertySet() {
        return null;
    }
}
