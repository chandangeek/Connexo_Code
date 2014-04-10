package com.energyict.mdc.device.config;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.config.impl.PartialOutboundConnectionTaskImpl;
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

    List<PartialOutboundConnectionTaskImpl> getPartialOutboundConnectionTasks();

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

    List<SecurityPropertySet> getSecurityPropertySets();

    SecurityPropertySetBuilder createSecurityPropertySet(String name);

    void removeSecurityPropertySet(SecurityPropertySet propertySet);
}