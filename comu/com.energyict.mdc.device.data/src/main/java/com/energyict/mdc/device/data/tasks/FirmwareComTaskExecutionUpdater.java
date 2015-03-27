package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;

/**
 * Copyrights EnergyICT
 * Date: 3/17/15
 * Time: 10:19 AM
 */
public interface FirmwareComTaskExecutionUpdater extends ComTaskExecutionUpdater<FirmwareComTaskExecutionUpdater, FirmwareComTaskExecution>{

    public FirmwareComTaskExecutionUpdater protocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties);

}
