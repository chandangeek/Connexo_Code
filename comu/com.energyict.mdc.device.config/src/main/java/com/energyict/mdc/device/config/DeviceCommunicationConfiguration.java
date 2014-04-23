package com.energyict.mdc.device.config;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.impl.PartialScheduledConnectionTaskImpl;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

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

    PartialScheduledConnectionTaskBuilder newPartialScheduledConnectionTask(String name, ConnectionTypePluggableClass connectionType, TimeDuration rescheduleRetryDelay, ConnectionStrategy connectionStrategy);

    PartialInboundConnectionTaskBuilder newPartialInboundConnectionTask(String name, ConnectionTypePluggableClass connectionType);

    PartialConnectionInitiationTaskBuilder newPartialConnectionInitiationTask(String name, ConnectionTypePluggableClass connectionType, TimeDuration rescheduleRetryDelay);

    List<PartialConnectionTask> getPartialConnectionTasks();

    List<PartialInboundConnectionTask> getPartialInboundConnectionTasks();

    List<PartialScheduledConnectionTaskImpl> getPartialOutboundConnectionTasks();

    List<PartialConnectionInitiationTask> getPartialConnectionInitiationTasks();

    void save();

    void delete();

    ProtocolDialectConfigurationProperties findOrCreateProtocolDialectConfigurationProperties(DeviceProtocolDialect protocolDialect);

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