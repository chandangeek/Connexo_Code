/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import com.elster.jupiter.pki.SecurityAccessorType;

import java.util.Optional;

public class DeviceSecurityAccessorType {

    private Optional<SecurityAccessorType> wrappingSecurityAccessor;
    private final SecurityAccessorType securityAccessor;


    public DeviceSecurityAccessorType(Optional<SecurityAccessorType> wrapperSecAccessorType, SecurityAccessorType secAccessorTypes) {
        wrappingSecurityAccessor = wrapperSecAccessorType;
        securityAccessor = secAccessorTypes;
    }

    public DeviceSecurityAccessorType(SecurityAccessorType wrapperSecAccessorType, SecurityAccessorType secAccessorTypes) {
        if (wrapperSecAccessorType != null) {
            wrappingSecurityAccessor = Optional.of(wrapperSecAccessorType);
        } else {
            wrappingSecurityAccessor = Optional.empty();
        }
        securityAccessor = secAccessorTypes;
    }

    public SecurityAccessorType getSecurityAccessor() {
        return securityAccessor;
    }

    public Optional<SecurityAccessorType> getWrappingSecurityAccessor() {
        return wrappingSecurityAccessor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeviceSecurityAccessorType)) {
            return false;
        }

        DeviceSecurityAccessorType that = (DeviceSecurityAccessorType) o;

        if (wrappingSecurityAccessor != null ? !wrappingSecurityAccessor.equals(that.wrappingSecurityAccessor) : that.wrappingSecurityAccessor != null) {
            return false;
        }
        return securityAccessor != null ? securityAccessor.equals(that.securityAccessor) : that.securityAccessor == null;
    }

    @Override
    public int hashCode() {
        int result = wrappingSecurityAccessor != null ? wrappingSecurityAccessor.hashCode() : 0;
        result = 31 * result + (securityAccessor != null ? securityAccessor.hashCode() : 0);
        return result;
    }
}
