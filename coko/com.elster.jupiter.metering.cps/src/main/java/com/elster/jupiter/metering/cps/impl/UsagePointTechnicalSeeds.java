package com.elster.jupiter.metering.cps.impl;

public enum UsagePointTechnicalSeeds {

    TABLE_NAME {
        @Override
        public String get() {
            return "RVK_CPS_UP_TECHNICAL";
        }
    },
    FK_CPS_DEVICE_TECHNICAL {
        @Override
        public String get() {
            return "FK_CPS_UP_TECHNICAL";
        }
    },
    COMPONENT_NAME {
        @Override
        public String get() {
            return "TECH";
        }
    };


    public abstract String get();
}
