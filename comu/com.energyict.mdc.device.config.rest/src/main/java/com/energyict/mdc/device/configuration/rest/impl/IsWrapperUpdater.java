package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityAccessorTypeUpdater;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.SecurityAccessorTypeOnDeviceType;

import javax.ws.rs.core.Response;
import java.util.Optional;

public class IsWrapperUpdater {


    private final SecurityAccessorTypeInfo securityAccessorTypeInfo;
    private final SecurityAccessorType securityAccessorType;
    private final SecurityAccessorTypeUpdater updater;
    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;

    public IsWrapperUpdater(SecurityAccessorTypeInfo securityAccessorTypeInfo, SecurityAccessorType securityAccessorType, SecurityAccessorTypeUpdater updater, ResourceHelper resourceHelper, ExceptionFactory exceptionFactory) {
        this.securityAccessorTypeInfo = securityAccessorTypeInfo;
        this.securityAccessorType = securityAccessorType;
        this.updater = updater;
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
    }

    public void update() {
        if (securityAccessorTypeInfo.isWrapper==false && securityAccessorType.isWrapper()) {
            // Unfortunately due to data model there is no other way then getting all device types
            // while the sec acc on dev type is in another data model compared to sec acc type and instantiated after it ... fucking ORM and data model... horrid
            // and yeah a refactoring -> moving all proper data model to PKI implies allot of work like taking care of upgraders (and also consider that merge is not possible between branches at this point in time)
            // ...perhaps another time
            for (DeviceType dt: resourceHelper.findDeviceTypes()) {
                for (SecurityAccessorTypeOnDeviceType sat: dt.getSecurityAccessors()) {
                    Optional<SecurityAccessorType> wrappingSecurityAccessor = sat.getDeviceSecurityAccessorType().getWrappingSecurityAccessor();
                    if (wrappingSecurityAccessor.isPresent() && wrappingSecurityAccessor.get().getId() == securityAccessorTypeInfo.id) {
                        throw exceptionFactory.newExceptionSupplier(Response.Status.CONFLICT, MessageSeeds.SECACC_WRAPPER_IN_USE, sat.getSecurityAccessorType().getName()).get();
                    }
                }
            }
        }
        updater.isWrapper(securityAccessorTypeInfo.isWrapper);
    }
}
