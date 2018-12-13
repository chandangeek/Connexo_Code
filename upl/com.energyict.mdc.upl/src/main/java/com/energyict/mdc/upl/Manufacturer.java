package com.energyict.mdc.upl;

import java.util.EnumSet;
import java.util.Set;

/**
 * Placeholder implementation
 */
public enum Manufacturer {

    Elster {
        @Override
        public Set<ElsterModel> getModels() {
            return EnumSet.allOf(ElsterModel.class);
        }
    },
    Eict {
        @Override
        public Set<EictModel> getModels() {
            return EnumSet.allOf(EictModel.class);
        }
    };

    abstract public Set<? extends Model> getModels();
}