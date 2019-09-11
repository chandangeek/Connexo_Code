package com.energyict.mdc.device.configuration.rest.impl;

public class DeviceSecurityAccessorTypeInfo {

    private final SecurityAccessorTypeInfo wrappingSecAccessor;
    private final SecurityAccessorTypeInfo secAccessor;

    public DeviceSecurityAccessorTypeInfo(SecurityAccessorTypeInfo wrappingSecAccessor, SecurityAccessorTypeInfo secAccessor) {
        this.wrappingSecAccessor = wrappingSecAccessor;
        this.secAccessor = secAccessor;
    }

    /**
     *
     * @return wrapper security accessor or null if there is none set
     */
    public SecurityAccessorTypeInfo getWrappingSecAccessor() {
        return wrappingSecAccessor;
    }

    public SecurityAccessorTypeInfo getSecAccessor() {
        return secAccessor;
    }
}
