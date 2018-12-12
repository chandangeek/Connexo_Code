/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.system.beans;

import com.elster.jupiter.system.BundleType;
import com.elster.jupiter.system.Component;

import java.util.Objects;

public class ComponentImpl implements Component {

    private String symbolicName;
    private String version;
    private BundleType bundleType;

    public ComponentImpl() {
    }

    @Override
    public String getSymbolicName() {
        return symbolicName;
    }

    public void setSymbolicName(String symbolicName) {
        this.symbolicName = symbolicName;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public BundleType getBundleType() {
        return bundleType;
    }

    public void setBundleType(BundleType bundleType) {
        this.bundleType = bundleType;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ComponentImpl)) {
            return false;
        }
        ComponentImpl o = (ComponentImpl) obj;

        return Objects.equals(this.symbolicName, o.symbolicName) &&
                Objects.equals(this.version, o.version) &&
                Objects.equals(this.bundleType, o.bundleType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbolicName, version, bundleType);
    }
}
