/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;

import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Implementation of a LoadProfileIdentifier that uniquely identifies a LoadProfile with its database ID.
 * <p>
 *
 * Date: 13/05/13
 * Time: 13:30
 */
@XmlRootElement
public final class LoadProfileIdentifierById implements LoadProfileIdentifier {

    private final ObisCode profileObisCode;
    private final DeviceIdentifier deviceIdentifier;
    private Long id;

    /**
     * Constructor only to be used by JSON (de)marshalling.
     */
    @SuppressWarnings("unused")
    public LoadProfileIdentifierById() {
        super();
        this.profileObisCode = null;
        this.deviceIdentifier = null;
    }

    public LoadProfileIdentifierById(Long id, ObisCode obisCode, DeviceIdentifier deviceIdentifier) {
        this.id = id;
        this.profileObisCode = obisCode;
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public ObisCode getProfileObisCode() {
        return profileObisCode;
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    @XmlAttribute
    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LoadProfileIdentifierById that = (LoadProfileIdentifierById) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    @Override
    public String toString() {
        return "load profile having id " + this.id;
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "DatabaseId";
        }

        @Override
        public Set<String> getRoles() {
            return new HashSet<>(Arrays.asList("databaseValue", "device", "obisCode"));
        }

        @Override
        public Object getValue(String role) {
            switch (role) {
                case "databaseValue":
                    return getId();
                case "device":
                    return getDeviceIdentifier();
                case "obisCode":
                    return getProfileObisCode();
                default:
                    throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
            }
        }
    }

}