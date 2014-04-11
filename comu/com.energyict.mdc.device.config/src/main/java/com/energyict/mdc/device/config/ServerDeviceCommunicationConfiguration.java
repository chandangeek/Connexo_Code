package com.energyict.mdc.device.config;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;

import java.util.List;
import java.util.Set;

/**
 * TODO at some point, this will have to be moved to the IMPL folder.
 * TODO Preferably when mdc-all is deleted so no external bundles require this anymore
 * TODO I'm sure @tgr will tell you all about it
 *
 * Copyrights EnergyICT
 * Date: 07/02/14
 * Time: 15:23
 */
public interface ServerDeviceCommunicationConfiguration  extends DeviceCommunicationConfiguration {
    /**
     * Tests if this configuration supports all current and future
     * DeviceMessageCategory DeviceMessageCategories.
     * If that is the case, then new categories that are
     * introduced in future versions of the mdc platform
     * will also be supported by this configuration.
     *
     * @return A flag that indicates if all current and future message categories are supported
     */
    public boolean supportsAllMessageCategories();

    /**
     * Gets the Set of DeviceMessageUserAction
     * that a user of the system MUST have
     * to be able to create any DeviceMessage.
     * Only applies when all DeviceMessageCategory DeviceMessageCategories are supported.
     *
     * @return The Set of DeviceMessageUserAction
     * @see #supportsAllMessageCategories()
     */
    public Set<DeviceMessageUserAction> getAllCategoriesUserActions();

    /**
     * Gets the specifications of which DeviceMessageCategory device message categories
     * and DeviceMessages
     * that are allowed to be used by Devices
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
     * categories (#supportsAllMessageCategories()), we check
     * if the message is supported by the DeviceProtocol and match if
     * the User is allowed to perform the message.
     *
     * @param deviceMessageSpec the messageSpec to check for authorization
     * @return true if this DeviceMessageSpec can be performed by the current user, false otherwise
     */
    public boolean isAuthorized(final DeviceMessageSpec deviceMessageSpec);

    /**
     * Checks the given ComTaskExecution is allowed to
     * be executed by the current user.
     *
     * @return true if the given ComTaskExecution is allowed to be executed by the given User
     */
//    public boolean isExecutableForCurrentUser(final ComTaskExecution comTaskExecution);

//    boolean isSupported(DeviceMessageSpec deviceMessageSpec);
}
