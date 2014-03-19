package com.energyict.mdc.device.config;

import com.energyict.mdc.device.config.impl.PartialConnectionInitiationTask;
import com.energyict.mdc.device.config.impl.PartialConnectionTask;
import com.energyict.mdc.device.config.impl.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.impl.PartialOutboundConnectionTask;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;

import java.util.List;

/**
 * Models the communication aspects of a {@link DeviceConfiguration}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-02-15 (10:27)
 */
public interface DeviceCommunicationConfiguration extends HasId {

    public DeviceConfiguration getDeviceConfiguration();

    void remove(PartialConnectionTask partialConnectionTask);

    void setSupportsAllMessageCategories(boolean supportAllMessageCategories);

    void addSecurityPropertySet(SecurityPropertySet securityPropertySet);

    PartialOutboundConnectionTaskBuilder createPartialOutboundConnectionTask();

    PartialInboundConnectionTaskBuilder createPartialInboundConnectionTask();

    PartialConnectionInitiationTaskBuilder createPartialConnectionInitiationTask();

    List<PartialConnectionTask> getPartialConnectionTasks();

    List<PartialInboundConnectionTask> getPartialInboundConnectionTasks();

    List<PartialOutboundConnectionTask> getPartialOutboundConnectionTasks();

    List<PartialConnectionInitiationTask> getPartialConnectionInitiationTasks();

    void addPartialConnectionTask(PartialConnectionTask partialConnectionTask);

    void save();

    void delete();

    ProtocolDialectConfigurationProperties createProtocolDialectConfigurationProperties(String name, DeviceProtocolDialect protocolDialect);

    /**
     * Gets the {@link com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties} that were created against this DeviceConfiguration.
     *
     * @return The List of ProtocolDialectConfigurationProperties
     */
    List<ProtocolDialectConfigurationProperties> getProtocolDialectConfigurationPropertiesList();
}