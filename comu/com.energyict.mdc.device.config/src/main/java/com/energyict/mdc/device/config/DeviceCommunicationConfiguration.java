package com.energyict.mdc.device.config;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.impl.PartialScheduledConnectionTaskImpl;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.tasks.ComTask;

import com.google.common.base.Optional;

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

    List<PartialScheduledConnectionTask> getPartialOutboundConnectionTasks();

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

    DeviceMessageEnablementBuilder createDeviceMessageEnablement(DeviceMessageId deviceMessageId);

    /**
     * Removed the DeviceMessageEnablement for the given DeviceMessageId
     *
     * @param deviceMessageId the deviceMessageId of the DeviceMessageEnablement which we need to remove
     *
     * @return true if we removed the required element, false otherwise
     */
    boolean removeDeviceMessageEnablement(DeviceMessageId deviceMessageId);

    public List<ComTaskEnablement> getComTaskEnablements();

    public Optional<ComTaskEnablement> getComTaskEnablementFor(ComTask comTask);

    /**
     * Starts a {@link ComTaskEnablementBuilder} that, once complete, will enable the execution
     * of the specified {@link ComTask} with the specified {@link SecurityPropertySet}
     * on all devices of this configuration.
     *
     * @param comTask The ComTask
     * @param securityPropertySet The SecurityPropertySet
     * @return The ComTaskEnablementBuilder that builds the enablement
     */
    public ComTaskEnablementBuilder enableComTask (ComTask comTask, SecurityPropertySet securityPropertySet);

    /**
     * Disables the execution of the specified {@link ComTask}
     * on all devices of this configuration.
     * This will effectively delete the related {@link ComTaskEnablement}.
     * Note that this will fail if the ComTask is already scheduled
     * to execute on Devices of this configuration.
     *
     * @param comTask The ComTask
     */
    public void  disableComTask (ComTask comTask);


    /**
     * Gets the specifications of which DeviceMessageCategory device message categories
     * and DeviceMessages
     * that are allowed to be used by Devices
     * of this configuration.
     *
     * @return The List of DeviceMessageEnablement
     */
    List<DeviceMessageEnablement> getDeviceMessageEnablements();

    /**
     * Checks if the current user is allowed to perform the provided
     * DeviceMessageSpec. Even if this config is marked to allow all
     * categories (#supportsAllMessageCategories()), we check
     * if the message is supported by the DeviceProtocol and match if
     * the User is allowed to perform the message.
     *
     * @param deviceMessageSpec the messageSpec to check for authorization
     * @return true if this DeviceMessageSpec can be performed by the current user, false otherwise
     */
    boolean isAuthorized(DeviceMessageSpec deviceMessageSpec);
}