/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl.kpi;

import java.util.EnumSet;
import java.util.Set;

enum MonitoredDataValidationKpiMemberTypes {
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

    ALLDATAVALIDATED {
        @Override
        public Set<DataValidationKpiMemberTypes> monitoredKpiMemberTypes() {
            return EnumSet.of(DataValidationKpiMemberTypes.ALLDATAVALIDATED);
        }
    },

    THRESHOLDVALIDATOR {
        @Override
        public Set<DataValidationKpiMemberTypes> monitoredKpiMemberTypes() {
            return EnumSet.of(DataValidationKpiMemberTypes.THRESHOLDVALIDATOR);
        }
    },

    MISSINGVALUESVALIDATOR {
        @Override
        public Set<DataValidationKpiMemberTypes> monitoredKpiMemberTypes() {
            return EnumSet.of(DataValidationKpiMemberTypes.MISSINGVALUESVALIDATOR);
        }
    },

    READINGQUALITIESVALIDATOR {
        @Override
        public Set<DataValidationKpiMemberTypes> monitoredKpiMemberTypes() {
            return EnumSet.of(DataValidationKpiMemberTypes.READINGQUALITIESVALIDATOR);
        }
    },

    REGISTERINCREASEVALIDATOR {
        @Override
        public Set<DataValidationKpiMemberTypes> monitoredKpiMemberTypes() {
            return EnumSet.of(DataValidationKpiMemberTypes.REGISTERINCREASEVALIDATOR);
        }
    };

    public abstract Set<DataValidationKpiMemberTypes> monitoredKpiMemberTypes();

}