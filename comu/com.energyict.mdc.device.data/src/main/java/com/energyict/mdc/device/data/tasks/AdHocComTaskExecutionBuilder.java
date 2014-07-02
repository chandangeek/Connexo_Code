package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;

/**
 * Builder that supports basic value setters for an {@link AdHocComTaskExecution}.
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2014-06-30 (14:53)
*/
public interface AdHocComTaskExecutionBuilder extends ComTaskExecutionBuilder<AdHocComTaskExecutionBuilder, AdHocComTaskExecution> {

    public AdHocComTaskExecutionBuilder protocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties);

}