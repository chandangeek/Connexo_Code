package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;

/**
 * Models a {@link ComTaskExecutionUpdater} for an {@link AdHocComTaskExecution}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-30 (11:41)
 */
public interface AdHocComTaskExecutionUpdaterRename extends ComTaskExecutionUpdater<AdHocComTaskExecutionUpdaterRename, AdHocComTaskExecution> {

    public AdHocComTaskExecutionUpdaterRename protocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties);

}