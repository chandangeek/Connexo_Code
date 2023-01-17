package com.energyict.mdc.device.data.impl.pki.tasks.command;


import com.energyict.mdc.common.device.config.SecurityAccessorTypeOnDeviceType;
import com.energyict.mdc.common.device.data.SecurityAccessor;

import java.util.Optional;

public class DeviceSecurityAccessorFilter implements Command {
    @Override
    public void run(SecurityAccessor securityAccessor) throws CommandAbortException {
        // not checking too many things while this filter should be used after device checks, so it must be linked to a device and so on ...
        Optional<SecurityAccessorTypeOnDeviceType> securityAccessorsOnDeviceType = securityAccessor.getDevice().getDeviceType().getSecurityAccessor(securityAccessor.getSecurityAccessorType());
        if (securityAccessorsOnDeviceType.isPresent() && securityAccessorsOnDeviceType.get().isRenewalConfigured()){
            return;
        }
        throw new CommandAbortException("Security renewal aborted while security accessor is not configured for renewal on this device");

    }
}
