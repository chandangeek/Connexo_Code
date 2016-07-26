package com.elster.jupiter.validation.impl.kpi;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum MonitoredDataValidationKpiMemberTypes {
    SUSPECT {
        @Override
        public Set<DataValidationKpiMemberTypes> monitoredKpiMemberTypes() {
            return EnumSet.complementOf(EnumSet.of(DataValidationKpiMemberTypes.SUSPECT));
        }
    },

    CHANNEL {
        @Override
        public Set<DataValidationKpiMemberTypes> monitoredKpiMemberTypes() {
            return EnumSet.of(DataValidationKpiMemberTypes.CHANNEL);
        }
    },

    REGISTER {
        @Override
        public Set<DataValidationKpiMemberTypes> monitoredKpiMemberTypes() {
            return EnumSet.of(DataValidationKpiMemberTypes.REGISTER);
        }
    },

    ALL_DATA_VALIDATED {
        @Override
        public Set<DataValidationKpiMemberTypes> monitoredKpiMemberTypes() {
            return EnumSet.of(DataValidationKpiMemberTypes.ALLDATAVALIDATED);
        }
    };

    public abstract Set<DataValidationKpiMemberTypes> monitoredKpiMemberTypes();

    public long calculateFrom(Map<DataValidationKpiMemberTypes, Long> statusCounters) {
        return this.monitoredKpiMemberTypes().stream()
                .mapToLong(statusCounters::get)
                .sum();
    }
}
