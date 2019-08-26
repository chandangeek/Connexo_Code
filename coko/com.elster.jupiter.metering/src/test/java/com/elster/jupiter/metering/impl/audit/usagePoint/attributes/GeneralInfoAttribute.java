/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.audit.usagePoint.attributes;

import com.elster.jupiter.metering.UsagePoint;


public enum GeneralInfoAttribute {

    NAME {
        @Override
        public String getName() {
            return "Name";
        }

        @Override
        public void setValueToObject(UsagePoint usagePoint, Object value) {
            usagePoint.setName(value.toString());
        }
    },

    LIFE_CYCLE {
        @Override
        public String getName() {
            return "Usage point life cycle";
        }

        @Override
        public void setValueToObject(UsagePoint usagePoint, Object value) {
            usagePoint.setLifeCycle(value.toString());
        }
    },

    SERVICE {
        @Override
        public String getName() {
            return "Service priority";
        }

        @Override
        public void setValueToObject(UsagePoint usagePoint, Object value) {
            usagePoint.setServicePriority(value.toString());
        }
    },

    ROUTE {
        @Override
        public String getName() {
            return "Read route";
        }

        @Override
        public void setValueToObject(UsagePoint usagePoint, Object value) {
            usagePoint.setReadRoute(value.toString());
        }
    };

    public abstract String getName();
    public abstract void setValueToObject(UsagePoint usagePoint, Object value);
}
