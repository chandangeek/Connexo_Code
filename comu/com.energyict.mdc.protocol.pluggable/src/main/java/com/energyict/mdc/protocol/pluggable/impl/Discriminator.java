/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.pluggable.Pluggable;
import com.energyict.mdc.pluggable.PluggableClassType;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.pluggable.PluggableClassLacksRelatedInterfaceException;

/**
 * Adds behavior to {@link PluggableClassType}s that relate to protocols.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-23 (11:05)
 */
enum Discriminator {
    CONNECTIONTYE {
        @Override
        protected int dbValue () {
            return 18;  // copied from com.energyict.mdw.core.PluggableClassType.CONNECTIONTYPE.getType();
        }

        @Override
        protected Class getInterface () {
            return ConnectionType.class;
        }

        @Override
        protected PluggableClassType getPluggableClassType() {
            return PluggableClassType.ConnectionType;
        }
    },
    DISCOVERYPROTOCOL {
        @Override
        protected int dbValue () {
            return 20;  // copied from com.energyict.mdw.core.PluggableClassType.DISCOVERYPROTOCOL.getType();
        }

        @Override
        protected Class getInterface () {
            return InboundDeviceProtocol.class;
        }

        @Override
        protected PluggableClassType getPluggableClassType() {
            return PluggableClassType.DiscoveryProtocol;
        }
    },
    DEVICEPROTOCOL {
        @Override
        protected int dbValue () {
            return 21;  // copied from com.energyict.mdw.core.PluggableClassType.DEVICEPROTOCOL.getType();
        }

        @Override
        protected Class getInterface () {
            return DeviceProtocol.class;
        }

        @Override
        protected PluggableClassType getPluggableClassType() {
            return PluggableClassType.DeviceProtocol;
        }

        @Override
        protected boolean doCheckInterfaceCompatibility (Pluggable pluggable) {
            return super.doCheckInterfaceCompatibility(pluggable)
                    || this.checkLegacyClassCompatibility(pluggable);
        }

        private boolean checkLegacyClassCompatibility (Pluggable pluggable) {
            return MeterProtocol.class.isAssignableFrom(pluggable.getClass())
                    || SmartMeterProtocol.class.isAssignableFrom(pluggable.getClass());
        }
    };

    protected abstract int dbValue();

    protected abstract <T extends Pluggable> Class getInterface ();

    protected abstract PluggableClassType getPluggableClassType ();

    protected boolean doCheckInterfaceCompatibility (Pluggable pluggable) {
        return this.getInterface().isAssignableFrom(pluggable.getClass());
    }

    protected void checkInterfaceCompatibility(Pluggable pluggable, Thesaurus thesaurus) {
        if (!this.doCheckInterfaceCompatibility(pluggable)) {
            throw new PluggableClassLacksRelatedInterfaceException(thesaurus, this.getPluggableClassType(),  this.getInterface(), pluggable);
        }
    }

}