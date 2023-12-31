/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.energyict.mdc.common.protocol.DeviceMessageCategory;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.common.protocol.DeviceProtocolDialect;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.PartialConnectionTask;

import aQute.bnd.annotation.ConsumerType;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Models the communication aspects of a {@link DeviceConfiguration}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-02-15 (10:27)
 */

@ConsumerType
public interface DeviceCommunicationConfiguration {

    void remove(PartialConnectionTask partialConnectionTask);

    void addSecurityPropertySet(SecurityPropertySet securityPropertySet);

    PartialScheduledConnectionTaskBuilder newPartialScheduledConnectionTask(String name, ConnectionTypePluggableClass connectionType, TimeDuration rescheduleRetryDelay, ConnectionStrategy connectionStrategy, ProtocolDialectConfigurationProperties configurationProperties);

    PartialInboundConnectionTaskBuilder newPartialInboundConnectionTask(String name, ConnectionTypePluggableClass connectionType, ProtocolDialectConfigurationProperties configurationProperties);

    PartialConnectionInitiationTaskBuilder newPartialConnectionInitiationTask(String name, ConnectionTypePluggableClass connectionType, TimeDuration rescheduleRetryDelay, ProtocolDialectConfigurationProperties configurationProperties);

    List<PartialConnectionTask> getPartialConnectionTasks();

    List<PartialInboundConnectionTask> getPartialInboundConnectionTasks();

    List<PartialScheduledConnectionTask> getPartialOutboundConnectionTasks();

    List<PartialConnectionInitiationTask> getPartialConnectionInitiationTasks();

    ProtocolDialectConfigurationProperties findOrCreateProtocolDialectConfigurationProperties(DeviceProtocolDialect protocolDialect);

    /**
     * Gets the {@link ProtocolDialectConfigurationProperties} that were created against this DeviceConfiguration.
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
     * @return true if we removed the required element, false otherwise
     */
    boolean removeDeviceMessageEnablement(DeviceMessageId deviceMessageId);

    List<ComTaskEnablement> getComTaskEnablements();

    Optional<ComTaskEnablement> getComTaskEnablementFor(ComTask comTask);

    /**
     * Starts a {@link ComTaskEnablementBuilder} that, once complete, will enable the execution
     * of the specified {@link ComTask} with the specified {@link SecurityPropertySet}
     * on all devices of this configuration.
     *
     * @param comTask             The ComTask
     * @param securityPropertySet The SecurityPropertySet
     * @return The ComTaskEnablementBuilder that builds the enablement
     */
    ComTaskEnablementBuilder enableComTask(ComTask comTask, SecurityPropertySet securityPropertySet);

    /**
     * Disables the execution of the specified {@link ComTask}
     * on all devices of this configuration.
     * This will effectively delete the related {@link ComTaskEnablement}.
     * Note that this will fail if the ComTask is already scheduled
     * to execute on Devices of this configuration.
     *
     * @param comTask The ComTask
     */
    void disableComTask(ComTask comTask);

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
     * DeviceMessage (by it's DeviceMessageId). Even if this config is marked to allow all
     * messages ({@link #supportsAllProtocolMessages}), we check
     * if the message is supported by the DeviceProtocol and match if
     * the User is allowed to perform the message.
     *
     * @param deviceMessageId the deviceMessageId to check for authorization
     * @return true if this DeviceMessage can be performed by the current user, false otherwise
     */
    boolean isAuthorized(DeviceMessageId deviceMessageId);

    /**
     * Returns the {@link DeviceMessageSpec} in the specified {@link DeviceMessageCategory}
     * that have been enabled on this configuration and that the user is authorized to execute.
     *
     * @param category The DeviceMessageCategory
     * @return The List of DeviceMessageSpec
     * @see #getDeviceMessageEnablements()
     * @see #isAuthorized(DeviceMessageId)
     */
    List<DeviceMessageSpec> getEnabledAndAuthorizedDeviceMessageSpecsIn(DeviceMessageCategory category);

    /**
     * Set whether or not this configuration should allow all protocol messages with the given deviceMessageUserActions.
     * <b>Note: Setting to true will remove all currently existing DeviceMessageEnablements</b>
     *
     * @param supportAllProtocolMessages indicates whether or not we allow all protocol messages
     * @param deviceMessageUserActions   the userActions for all protocol messages
     */
    void setSupportsAllProtocolMessagesWithUserActions(boolean supportAllProtocolMessages, DeviceMessageUserAction... deviceMessageUserActions);

    /**
     * Tests whether or not all protocol messages are supported.
     *
     * @return true if all protocol messages are supported, false otherwise
     */
    boolean supportsAllProtocolMessages();

    /**
     * Gets the Set of {@link DeviceMessageUserAction} when all protocol messages are supported.
     *
     * @return The Set of DeviceMessageUserAction
     * @see #supportsAllProtocolMessages()
     */
    Set<DeviceMessageUserAction> getAllProtocolMessagesUserActions();

}