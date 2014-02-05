package com.energyict.mdc.device.config;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.NamedBusinessObject;
import com.energyict.mdc.devices.configuration.DeviceCommunicationConfiguration;
import com.energyict.mdc.protocol.api.device.Device;
import com.energyict.mdw.core.configchange.DeviceConfigurationChanges;
import com.energyict.mdw.shadow.DeviceConfigurationShadow;
import com.energyict.mdw.task.CreateDeviceTransaction;
import com.energyict.mdw.xml.Exportable;

import java.sql.SQLException;
import java.util.List;

/**
 * User: gde
 * Date: 5/11/12
 */
public interface DeviceConfiguration {

    long getId();

    /**
     * Gets the name of the DeviceConfiguration which will be unique within a DeviceType
     *
     * @return the name of the DeviceConfiguration
     */
    String getName();

    /**
     * Returns a description of the receiver
     *
     * @return description
     */
    String getDescription();

    /**
     * Returns the prototype device for this device config
     *
     * @return the prototype device or null
     */
    Device getPrototypeDevice();

    /**
     * Returns the ID of the prototype device
     *
     * @return the ID of the prototype device or 0
     */
    int getPrototypeId();

    /**
     * Returns the <code>DeviceType</code> this device config belongs to
     *
     * @return the <code>DeviceType</code> this device config belongs to
     */
    DeviceType getDeviceType();

    List<RegisterSpec> getRegisterSpecs();

    List<ChannelSpec> getChannelSpecs();

    List<LoadProfileSpec> getLoadProfileSpecs();

    List<LogBookSpec> getLogBookSpecs();

    /**
     * tests if the receiver is active
     *
     * @return true if active, false otherwise
     */
    boolean getActive();

    void activate() throws SQLException, BusinessException;

    void deactivate() throws SQLException, BusinessException;

    boolean hasLogBookSpecForConfig(int logBookTypeId, int updateId);

    /**
     * Gets the details of this DeviceConfiguration that relate to communication.
     *
     * @return The DeviceCommunicationConfiguration
     */
    public DeviceCommunicationConfiguration getCommunicationConfiguration();

    /**
     * Returns a new {@link CreateDeviceTransaction transaction}
     * that allows the creation of a new {@link Device} from this DeviceConfiguration
     * according to the specification that are laid out in the prototype.
     * All of the properties and settings of the prototype are copied
     * into this transaction. If there is not prototype then obviously
     * only the specifications of this DeviceConfiguration are copied
     * into this transaction.
     *
     * @return The CreateDeviceTransaction
     */
    public CreateDeviceTransaction newDeviceTransaction();

    /**
     * Returns a new {@link CreateDeviceTransaction transaction}
     * that allows the creation of a clone of the specified {@link Device}.
     * All of the properties and settings of the prototype are copied
     * into this transaction.
     *
     * @param device The original Device
     * @return The cloned device
     */
    public CreateDeviceTransaction newDeviceTransactionForCloning(Device device);

    /**
     * Creates a DeviceConfigurationChanges object that allows to perform a device configuration change
     * from a device having this configuration to a device having the target configuration
     * (cf. the method Device.changeConfiguration())
     */
    DeviceConfigurationChanges constructDeviceConfigurationChanges(DeviceConfiguration targetConfiguration);

}
