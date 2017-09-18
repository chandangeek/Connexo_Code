/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides an implementation for the {@link DeviceIdentifier} interface
 * that uses the unique mRID of the device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-03-21 (15:10)
 */
@XmlRootElement
public final class DeviceIdentifierByMRID implements DeviceIdentifier {

    private String mrid;

    /**
     * Constructor only to be used by JSON (de)marshalling or in unit tests
     */
    public DeviceIdentifierByMRID() {
    }

    public DeviceIdentifierByMRID(String mrid) {
        this();
        this.mrid = mrid;
    }

    @Override
    public String toString() {
        return "device having MRID " + this.mrid;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeviceIdentifierByMRID)) {
            return false;
        }

        DeviceIdentifierByMRID that = (DeviceIdentifierByMRID) o;

        return !(mrid != null ? !mrid.equals(that.mrid) : that.mrid != null);

    }

    @Override
    public int hashCode() {
        return mrid != null ? mrid.hashCode() : 0;
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "mRID";
        }

        @Override
        public Set<String> getRoles() {
            return new HashSet<>(Collections.singletonList("databaseValue"));
        }

        @Override
        public Object getValue(String role) {
            switch (role) {
                case "databaseValue": {
                    return mrid;
                }
                default: {
                    throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
                }
            }
        }
    }
}