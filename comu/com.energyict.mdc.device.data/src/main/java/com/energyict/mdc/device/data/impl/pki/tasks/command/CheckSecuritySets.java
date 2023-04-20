package com.energyict.mdc.device.data.impl.pki.tasks.command;

import com.elster.jupiter.pki.SecurityAccessorType;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.SecurityAccessor;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CheckSecuritySets implements Command {

    private final Logger logger;

    public CheckSecuritySets(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void run(SecurityAccessor securityAccessor) throws CommandErrorException, CommandAbortException {
        Device device = securityAccessor.getDevice();
        if (device == null) {
            throw new CommandErrorException("No device attached to security accessor:" + securityAccessor);
        }
        SecurityAccessorType keyAccessorType = securityAccessor.getSecurityAccessorType();
        logger.log(Level.INFO, "Checking security sets, Type=" + keyAccessorType.getName()
                + " Device=" + device.getName());
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        if (deviceConfiguration == null) {
            throw new CommandErrorException("No device configuration found for device:" + securityAccessor);
        }

        if (deviceConfiguration.isConfigured(keyAccessorType)) {
            logger.log(Level.INFO, "Used by security set");
            return;
        }

        throw new CommandAbortException("Not used by security set, stopping renew action for security accessor:" + securityAccessor);
    }
}
