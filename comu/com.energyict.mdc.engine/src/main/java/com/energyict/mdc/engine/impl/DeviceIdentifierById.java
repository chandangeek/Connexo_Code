/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Provides an implementation for the {@link DeviceIdentifier} interface
 * that uses the unique database id of the device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-03-21 (15:10)
 */
@XmlRootElement
public final class DeviceIdentifierById implements DeviceIdentifier {

    private long id;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    public DeviceIdentifierById() {
    }

    public static DeviceIdentifierById from(long id) {
        return new DeviceIdentifierById(id);
    }

    private DeviceIdentifierById(long id) {
        this();
        this.id = id;
    }

    @Override
    public String toString() {
        return "device having database id " + this.id;
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
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceIdentifierById that = (DeviceIdentifierById) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "DatabaseId";
        }

        @Override
        public Set<String> getRoles() {
            return new HashSet<>(Collections.singletonList("databaseValue"));
        }


        @Override
        public Object getValue(String role) {
            if ("databaseValue".equals(role)) {
                return id;
            } else {
                throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
            }
        }
    }

}