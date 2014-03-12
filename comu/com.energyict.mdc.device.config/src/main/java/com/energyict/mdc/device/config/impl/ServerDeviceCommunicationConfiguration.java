package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.device.config.DeviceMessageUserAction;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;

import java.util.List;
import java.util.Set;

/**
 * Copyrights EnergyICT
 * Date: 07/02/14
 * Time: 15:23
 */
public interface ServerDeviceCommunicationConfiguration  extends DeviceCommunicationConfiguration {
    public List<SecurityPropertySet> getSecurityPropertySets();

    public List<ComTaskEnablement> getEnabledComTasks();

    /**
     * Gets all the {@link PartialConnectionTask} that were created against this DeviceCommunicationConfiguration.
     *
     * @return The List of PartialConnectionTask
     */
    public List<PartialConnectionTask> getPartialConnectionTasks();

    /**
     * Gets the {@link PartialOutboundConnectionTask}s that were created against this DeviceConfiguration.
     *
     * @return The List of PartialOutboundConnectionTask
     */
    public List<PartialOutboundConnectionTask> getPartialOutboundConnectionTasks();

    /**
     * Gets the {@link PartialInboundConnectionTask} that is created against this DeviceConfiguration.
     *
     * @return The PartialInboundConnectionTask
     */
    public List<PartialInboundConnectionTask> getPartialInboundConnectionTasks();

    /**
     * Gets the {@link PartialConnectionInitiationTask}s that were created against this DeviceConfiguration.
     *
     * @return The List of PartialConnectionInitiationTask
     */
    public List<PartialConnectionInitiationTask> getPartialConnectionInitiationTasks();

    /**
     * Gets the {@link ProtocolDialectConfigurationProperties} that were created against this DeviceConfiguration.
     *
     * @return The List of ProtocolDialectConfigurationProperties
     */
    public List<ProtocolDialectConfigurationProperties> getProtocolDialectConfigurationPropertiesList();

    /**
     * Tests if this configuration supports all current and future
     * {@link com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory DeviceMessageCategories}.
     * If that is the case, then new categories that are
     * introduced in future versions of the mdc platform
     * will also be supported by this configuration.
     *
     * @return A flag that indicates if all current and future message categories are supported
     */
    public boolean supportsAllMessageCategories();

    /**
     * Gets the Set of {@link DeviceMessageUserAction}
     * that a user of the system MUST have
     * to be able to create any {@link com.energyict.mdc.protocol.api.device.messages.DeviceMessage}.
     * Only applies when all {@link com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory DeviceMessageCategories} are supported.
     *
     * @return The Set of DeviceMessageUserAction
     * @see #supportsAllMessageCategories()
     */
    public Set<DeviceMessageUserAction> getAllCategoriesUserActions();

    /**
     * Gets the specifications of which {@link com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory device message categories}
     * and {@link com.energyict.mdc.protocol.api.device.messages.DeviceMessage}s
     * that are allowed to be used by {@link com.energyict.mdc.protocol.api.device.Device}s
     * of this configuration.
     *
     * @return The List of DeviceMessageEnablement
     */
    public List<DeviceMessageEnablement> getDeviceMessageEnablements();


    /**
     * Provide a set of DeviceMessageSpecs which are executable by the current user.
     *
     * @return the allowed executable DeviceMessageSpecs
     */
//    public List<DeviceMessageSpec> getAllowedDeviceMessageSpecsForCurrentUser();

    /**
     * Checks if the current user is allowed to perform the provided
     * DeviceMessageSpec. Even if this config is marked to allow all
     * categories ({@link #supportsAllMessageCategories()}), we check
     * if the message is supported by the DeviceProtocol and match if
     * the User is allowed to perform the message.
     *
     * @param deviceMessageSpec the messageSpec to check for authorization
     * @return true if this DeviceMessageSpec can be performed by the current user, false otherwise
     */
    public boolean isAuthorized(final DeviceMessageSpec deviceMessageSpec);

    /**
     * Checks the given {@link ComTaskExecution} is allowed to
     * be executed by the current user.
     *
     * @return true if the given ComTaskExecution is allowed to be executed by the given User
     */
//    public boolean isExecutableForCurrentUser(final ComTaskExecution comTaskExecution);

//    boolean isSupported(DeviceMessageSpec deviceMessageSpec);
}
