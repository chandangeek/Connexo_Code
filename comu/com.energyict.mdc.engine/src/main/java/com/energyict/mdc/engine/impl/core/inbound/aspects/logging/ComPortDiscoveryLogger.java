package com.energyict.mdc.engine.impl.core.inbound.aspects.logging;

import com.energyict.comserver.logging.Configuration;
import com.energyict.comserver.logging.LogLevel;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;

/**
 * Defines all log messages for inbound communication.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-24 (13:42)
 */
public interface ComPortDiscoveryLogger {

    /**
     * Logs the start of inbound discovery to find out
     * which {@link com.energyict.mdc.protocol.api.device.BaseDevice device}
     * has started the communication session.
     *
     * @param discoveryProtocolClassName The class name of the {@link com.energyict.mdc.protocol.inbound.InboundDeviceProtocol}
     * @param comPort The {@link com.energyict.mdc.engine.model.InboundComPort} on which the communication was started
     */
    @Configuration(format = "Device identification discovery started by class ''{0}'' ...", logLevel = LogLevel.INFO)
    public void discoveryStarted (String discoveryProtocolClassName, InboundComPort comPort);

    /**
     * Logs the failure of inbound discovery to find out
     * which {@link com.energyict.mdc.protocol.api.device.BaseDevice device}
     * has started the communication session.
     *
     * @param discoveryProtocolClassName The class name of the {@link com.energyict.mdc.protocol.inbound.InboundDeviceProtocol}
     * @param comPort The InboundComPort on which the communication was started
     */
    @Configuration(format = "Device identification discovery by class ''{0}'' failed", logLevel = LogLevel.ERROR)
    public void discoveryFailed (String discoveryProtocolClassName, InboundComPort comPort);

    /**
     * Logs that the {@link InboundDeviceProtocol}
     * discovered only a {@link DeviceIdentifier}.
     *
     * @param deviceIdentifier The DeviceIdentifier discovered by the InboundDeviceProtocol
     * @param comPort The {@link com.energyict.mdc.engine.model.InboundComPort} on which the communication was started
     */
    @Configuration(format = "Device identification discovered on port ''{1}'' : ''{0}''", logLevel = LogLevel.INFO)
    public void discoveryFoundIdentifierOnly (DeviceIdentifier deviceIdentifier, InboundComPort comPort);

    /**
     * Logs that the {@link InboundDeviceProtocol}
     * discovered both a {@link DeviceIdentifier} and measurement data.
     *
     * @param deviceIdentifier The DeviceIdentifier discovered by the InboundDeviceProtocol
     * @param comPort The {@link com.energyict.mdc.engine.model.InboundComPort} on which the communication was started
     */
    @Configuration(format = "Device identification and data discovered on port ''{1}'' : ''{0}''", logLevel = LogLevel.INFO)
    public void discoveryFoundIdentifierAndData (DeviceIdentifier deviceIdentifier, InboundComPort comPort);

    /**
     * Logs that the {@link DeviceIdentifier identifier} discoverd by the
     * {@link InboundDeviceProtocol}
     * does not identify an existing {@link com.energyict.mdc.protocol.api.device.BaseDevice device}.
     *
     * @param deviceIdentifier The DeviceIdentifier discovered by the InboundDeviceProtocol
     * @param comPort The InboundComPort on which the communication was started
     */
    @Configuration(format = "Device identified by ''{0}'' that started communication on port ''{1}'' was not found", logLevel = LogLevel.ERROR)
    public void deviceNotFound (DeviceIdentifier deviceIdentifier, InboundComPort comPort);

    /**
     * Logs that the {@link com.energyict.mdc.protocol.api.device.BaseDevice device} identified
     * by the {@link DeviceIdentifier} discoverd by the
     * {@link InboundDeviceProtocol}
     * is not configured for inbound communication.
     *
     * @param deviceIdentifier The DeviceIdentifier discovered by the InboundDeviceProtocol
     * @param comPort The InboundComPort on which the communication was started
     */
    @Configuration(format = "Device identified by ''{0}'' that started communication on port ''{1}'' is not configured for inbound communication", logLevel = LogLevel.ERROR)
    public void deviceNotConfiguredForInboundCommunication (DeviceIdentifier deviceIdentifier, InboundComPort comPort);

    /**
     * Logs that the {@link com.energyict.mdc.protocol.api.device.BaseDevice device} identified
     * by the {@link DeviceIdentifier identifier} discoverd by the
     * {@link InboundDeviceProtocol}
     * requires that data is encrypted.
     *
     * @param deviceIdentifier The DeviceIdentifier discovered by the InboundDeviceProtocol
     * @param comPort The InboundComPort on which the communication was started
     */
    @Configuration(format = "Device identified by ''{0}'' that started communication on port ''{1}'' is NOT sending encrypted information as expected", logLevel = LogLevel.ERROR)
    public void deviceRequiresEncryptedData (DeviceIdentifier deviceIdentifier, InboundComPort comPort);

    /**
     * Logs that the server is temporarily too busy to handle
     * the inbound communication from the {@link com.energyict.mdc.protocol.api.device.BaseDevice device}
     * identified by the {@link DeviceIdentifier} discoverd by the
     * {@link InboundDeviceProtocol}.
     *
     * @param deviceIdentifier The DeviceIdentifier discovered by the InboundDeviceProtocol
     * @param comPort The {@link com.energyict.mdc.engine.model.InboundComPort} on which the communication was started
     */
    @Configuration(format = "Server is temporarily too busy to handle communication started by Device (id {0}) on port ''{1}''", logLevel = LogLevel.INFO)
    public void serverTooBusy (DeviceIdentifier deviceIdentifier, InboundComPort comPort);

    /**
     * Logs that the {@link com.energyict.mdc.protocol.api.device.BaseDevice device} identified
     * by the {@link DeviceIdentifier identifier} discoverd by the
     * {@link InboundDeviceProtocol}
     * was found.
     *
     * @param deviceIdentifier The DeviceIdentifier discovered by the InboundDeviceProtocol
     * @param comPort The InboundComPort on which the communication was started
     */
    @Configuration(format = "Device identified by ''{0}'' successfully started communication on port ''{1}''", logLevel = LogLevel.INFO)
    public void deviceIdentified (DeviceIdentifier deviceIdentifier, InboundComPort comPort);

    /**
     * Logs that a number of data elements that were collected by the
     * {@link InboundDeviceProtocol}
     * were filtered because that type of data was not configured
     * to be collected on any of the inbound {@link com.energyict.mdc.tasks.ComTaskExecution}.
     *
     * @param deviceIdentifier The DeviceIdentifier discovered by the InboundDeviceProtocol
     * @param comPort The InboundComPort on which the communication was started
     * @param numberOfItemsFiltered The number of items that was filtered
     */
    @Configuration(format = "The data that was collected during discovery of device identified by ''{0}'' on port ''{1}'' was filtered (number of elements filtered: {2})", logLevel = LogLevel.INFO)
    public void collectedDataWasFiltered (DeviceIdentifier deviceIdentifier, InboundComPort comPort, int numberOfItemsFiltered);

}