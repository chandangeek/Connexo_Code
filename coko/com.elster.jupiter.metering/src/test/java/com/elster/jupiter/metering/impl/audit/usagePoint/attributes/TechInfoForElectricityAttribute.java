/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.audit.usagePoint.attributes;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.metering.ElectricityDetailBuilder;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.units.Quantity;


public enum TechInfoForElectricityAttribute {

    GROUNDED {
        @Override
        public String getName() {
            return "Grounded";
        }

        @Override
        public void setValueToObject(ElectricityDetailBuilder electricityDetailBuilder, Object value) {
            electricityDetailBuilder.withGrounded((YesNoAnswer)value);
        }
    },

    NOMINAL_VOLTAGE {
        @Override
        public String getName() {
            return "Nominal voltage";
        }

        @Override
        public void setValueToObject(ElectricityDetailBuilder electricityDetailBuilder, Object value) {
            electricityDetailBuilder.withNominalServiceVoltage((Quantity)value);
        }
    },

    PHASE_CODE {
        @Override
        public String getName() {
            return "Phase code";
        }

        @Override
        public void setValueToObject(ElectricityDetailBuilder electricityDetailBuilder, Object value) {
            electricityDetailBuilder.withPhaseCode((PhaseCode) value);
        }
    },

    RATED_POVER {
        @Override
        public String getName() {
            return "Rated power";
        }

        @Override
        public void setValueToObject(ElectricityDetailBuilder electricityDetailBuilder, Object value) {
            electricityDetailBuilder.withRatedPower((Quantity) value);
        }
    },

    RATED_CURRENT {
        @Override
        public String getName() {
            return "Rated current";
        }

        @Override
        public void setValueToObject(ElectricityDetailBuilder electricityDetailBuilder, Object value) {
            electricityDetailBuilder.withRatedCurrent((Quantity) value);
        }
    },

    ESTIMATED_LOAD {
        @Override
        public String getName() {
            return "Estimated load";
        }

        @Override
        public void setValueToObject(ElectricityDetailBuilder electricityDetailBuilder, Object value) {
            electricityDetailBuilder.withEstimatedLoad((Quantity) value);
        }
    },

    LIMITER {
        @Override
        public String getName() {
            return "Limiter";
        }

        @Override
        public void setValueToObject(ElectricityDetailBuilder electricityDetailBuilder, Object value) {
                electricityDetailBuilder.withLimiter((YesNoAnswer)value);
        }
    },

    LOAD_LIMITER_TYPE {
        @Override
        public String getName() {
            return "Load limiter type";
        }

        @Override
        public void setValueToObject(ElectricityDetailBuilder electricityDetailBuilder, Object value) {
            electricityDetailBuilder.withLoadLimiterType((String)value.toString());
        }
    },

    LOAD_LIMIT {
        @Override
        public String getName() {
            return "Load limit";
        }

        @Override
        public void setValueToObject(ElectricityDetailBuilder electricityDetailBuilder, Object value) {
            electricityDetailBuilder.withLoadLimit((Quantity)value);
        }
    },

    COLLAR {
        @Override
        public String getName() {
            return "Collar";
        }

        @Override
        public void setValueToObject(ElectricityDetailBuilder electricityDetailBuilder, Object value) {
                electricityDetailBuilder.withCollar((YesNoAnswer)value);
        }
    },

    INTERRUPTIBLE {
        @Override
        public String getName() {
            return "Interruptible";
        }

        @Override
        public void setValueToObject(ElectricityDetailBuilder electricityDetailBuilder, Object value) {
            electricityDetailBuilder.withInterruptible((YesNoAnswer)value);
        }
    },;

    public abstract String getName();
    public abstract void setValueToObject(ElectricityDetailBuilder electricityDetailBuilder, Object value);
}
