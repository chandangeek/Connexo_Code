/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cps.impl;

enum UsagePointTechnicalSeeds {

    TABLE_NAME {
        @Override
        public String get() {
            return "MTC_CPS_UP_TECHNICAL";
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
            return "TEH";
        }
    };


    public abstract String get();

}