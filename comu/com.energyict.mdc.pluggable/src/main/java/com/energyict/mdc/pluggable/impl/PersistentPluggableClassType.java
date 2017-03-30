/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.impl;

import com.energyict.mdc.pluggable.PluggableClassType;

/**
 * The persistent version of {@link PluggableClassType}
 * that adds "reserved" values to make sure that the old
 * EIServer database schemas containing existing
 * pluggable classes can be read without problems.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-20 (17:30)
 */
public enum PersistentPluggableClassType {
    OBSOLETE_IMPORT, OBSOLETE_EXPORT, OBSOLETE_FOLDERACTION, OBSOLETE_REPORTER, OBSOLETE_VALIDATOR, OBSOLETE_EVENTHANDLER,
    OBSOLETE_SERVICEREQUESTHANDLER, OBSOLETE_PORTALWIDGET, OBSOLETE_MESSAGESERVICERHANDLER, OBSOLETE_REPORTCONTENTPROVIDER, OBSOLETE_RTUREGISTERVALIDATOR,
    OBSOLETE_INVOICEACTION, OBSOLETE_CONSUMPTIONPERIODACTION, OBSOLETE_WORKFLOWSERVICE, OBSOLETE_CUSTOM_MEASUREMENTSTRATEGY, OBSOLETE_KPICALCULATOR,
    OBSOLETE_RTUACTION,
    // 18
    CONNECTION_TYPE {
        @Override
        public PluggableClassType toActualType() {
            return PluggableClassType.ConnectionType;
        }
    },
    OBSOLETE_NOTRACEOFWHATTHISUSEDTOBE,
    // 20
    DISCOVERY_PROTOCOL {
        @Override
        public PluggableClassType toActualType() {
            return PluggableClassType.DiscoveryProtocol;
        }
    },
    // 21
    DEVICE_PROTOCOL {
        @Override
        public PluggableClassType toActualType() {
            return PluggableClassType.DeviceProtocol;
        }
    },
    OBSOLETE_ACTIONINTERCEPTOR;

    public PluggableClassType toActualType() {
        return null;
    }

    public static PersistentPluggableClassType forActualType (PluggableClassType actualType) {
        for (PersistentPluggableClassType persistentType : values()) {
            if (actualType == persistentType.toActualType()) {
                return persistentType;
            }
        }
        throw new RuntimeException("No applicable PersistentPluggableClassType found for " + actualType);
    }

}