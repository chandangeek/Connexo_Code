/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.impl.logging.Configuration;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

/**
 * Defines all log messages for inbound communication.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-24 (13:42)
 */
public interface ComPortDiscoveryLogger {

    /**
     * Logs the start of inbound discovery to find out
     * which {@link com.energyict.mdc.upl.meterdata.Device device}
     * has started the communication session.
     *
     * @param discoveryProtocolClassName The class name of the {@link com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol}
     * @param comPort The {@link com.energyict.mdc.engine.config.InboundComPort} on which the communication was started
     */
    @Configuration(format = "Device identification discovery started by class ''{0}'' ...", logLevel = LogLevel.INFO)
    void discoveryStarted(String discoveryProtocolClassName, InboundComPort comPort);

    /**
     * Logs the failure of inbound discovery to find out
     * which {@link com.energyict.mdc.upl.meterdata.Device device}
     * has started the communication session.
     *
     * @param discoveryProtocolClassName The class name of the {@link com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol}
     * @param comPort The InboundComPort on which the communication was started
     */
    @Configuration(format = "Device identification discovery by class ''{0}'' failed", logLevel = LogLevel.ERROR)
    void discoveryFailed(String discoveryProtocolClassName, InboundComPort comPort);

    /**
     * Logs that the {@link InboundDeviceProtocol}
     * discovered only a {@link DeviceIdentifier}.
     *
     * @param deviceIdentifier The DeviceIdentifier discovered by the InboundDeviceProtocol
     * @param comPort The {@link com.energyict.mdc.engine.config.InboundComPort} on which the communication was started
     */
    @Configuration(format = "Device identification discovered on port ''{1}'' : ''{0}''", logLevel = LogLevel.INFO)
    void discoveryFoundIdentifierOnly(DeviceIdentifier deviceIdentifier, InboundComPort comPort);

    /**
     * Logs that the {@link InboundDeviceProtocol}
     * discovered both a {@link DeviceIdentifier} and measurement data.
     *
     * @param deviceIdentifier The DeviceIdentifier discovered by the InboundDeviceProtocol
     * @param comPort The {@link com.energyict.mdc.engine.config.InboundComPort} on which the communication was started
     */
    @Configuration(format = "Device identification and data discovered on port ''{1}'' : ''{0}''", logLevel = LogLevel.INFO)
    void discoveryFoundIdentifierAndData(DeviceIdentifier deviceIdentifier, InboundComPort comPort);

    /**
     * Logs that the {@link DeviceIdentifier identifier} discoverd by the
     * {@link InboundDeviceProtocol}
     * does not identify an existing {@link com.energyict.mdc.upl.meterdata.Device device}.
     *
     * @param deviceIdentifier The DeviceIdentifier discovered by the InboundDeviceProtocol
     * @param comPort The InboundComPort on which the communication was started
     */
    @Configuration(format = "Device identified by ''{0}'' that started communication on port ''{1}'' was not found", logLevel = LogLevel.ERROR)
    void deviceNotFound(DeviceIdentifier deviceIdentifier, InboundComPort comPort);

    /**
     * Logs that the {@link com.energyict.mdc.upl.meterdata.Device device} identified
     * by the {@link DeviceIdentifier} discoverd by the
     * {@link InboundDeviceProtocol}
     * is not configured for inbound communication.
     *
     * @param deviceIdentifier The DeviceIdentifier discovered by the InboundDeviceProtocol
     * @param comPort The InboundComPort on which the communication was started
     */
    @Configuration(format = "Device identified by ''{0}'' that started communication on port ''{1}'' is not configured for inbound communication", logLevel = LogLevel.ERROR)
    void deviceNotConfiguredForInboundCommunication(DeviceIdentifier deviceIdentifier, InboundComPort comPort);

    /**
     * Logs that the {@link com.energyict.mdc.upl.meterdata.Device device} identified
     * by the {@link DeviceIdentifier identifier} discoverd by the
     * {@link InboundDeviceProtocol}
     * requires that data is encrypted.
     *
     * @param deviceIdentifier The DeviceIdentifier discovered by the InboundDeviceProtocol
     * @param comPort The InboundComPort on which the communication was started
     */
    @Configuration(format = "Device identified by ''{0}'' that started communication on port ''{1}'' is NOT sending encrypted information as expected", logLevel = LogLevel.ERROR)
    void deviceRequiresEncryptedData(DeviceIdentifier deviceIdentifier, InboundComPort comPort);

    /**
     * Logs that the server is temporarily too busy to handle
     * the inbound communication from the {@link com.energyict.mdc.upl.meterdata.Device device}
     * identified by the {@link DeviceIdentifier} discoverd by the
     * {@link InboundDeviceProtocol}.
     *
     * @param deviceIdentifier The DeviceIdentifier discovered by the InboundDeviceProtocol
     * @param comPort The {@link com.energyict.mdc.engine.config.InboundComPort} on which the communication was started
     */
    @Configuration(format = "Server is temporarily too busy to handle communication started by Device (id {0}) on port ''{1}''", logLevel = LogLevel.INFO)
    void serverTooBusy(DeviceIdentifier deviceIdentifier, InboundComPort comPort);

    /**
     * Logs that the {@link com.energyict.mdc.upl.meterdata.Device device} identified
     * by the {@link DeviceIdentifier identifier} discoverd by the
     * {@link InboundDeviceProtocol}
     * was found.
     *
     * @param deviceIdentifier The DeviceIdentifier discovered by the InboundDeviceProtocol
     * @param comPort The InboundComPort on which the communication was started
     */
    @Configuration(format = "Device identified by ''{0}'' successfully started communication on port ''{1}''", logLevel = LogLevel.INFO)
    void deviceIdentified(DeviceIdentifier deviceIdentifier, InboundComPort comPort);

    /**
     * Logs that a number of data elements that were collected by the
     * {@link InboundDeviceProtocol}
     * were filtered because that type of data was not configured
     * to be collected on any of the inbound ComTaskExecution.
     *  @param dataType         The type of data that will be dropped
     * @param deviceIdentifier The DeviceIdentifier discovered by the InboundDeviceProtocol
     * @param comPort The InboundComPort on which the communication was started
     */
    @Configuration(format = "The data of type ''{0}'' that was collected during discovery of device ''{1}'' on port ''{2}'' was dropped, because it is not configured on any of the inbound comtasks.", logLevel = LogLevel.WARN)
    void collectedDataWasFiltered(String dataType, DeviceIdentifier deviceIdentifier, ComPort comPort);

}