package com.energyict.mdc.upl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Defines a common set of Capabilities a DeviceProtocol can have.
 * <table border="1">
 * <head>
 * <tr><th width="25%">Capability</th><th width="75%">Description</th></tr>
 * <head>
 * <body>
 * <tr><td>{@link #PROTOCOL_SESSION Protocol Session}</td><td>A device that that can initiate its
 * own protocol session but is not necessarily also in control of the communication connection
 * (e.g. when connected to a gateway)</td></tr>
 * <tr><td>{@link #PROTOCOL_MASTER Protocol Master}</td><td>Enables a device to become available
 * as "Master" for the logical slave devices</td></tr>
 * <tr><td>{@link #PROTOCOL_SLAVE Protocol Slave}</td><td>A device is not initiating any protocol
 * session and is relying on a "Protocol Master" device to be its master and be read/contacted in
 * his place (=mirror)</td></tr>
 * </body>
 * </table>
 * <p/>
 *
 * Date: 26/11/12
 * Time: 15:31
 *
 * @see DeviceCommunicationFunction DeviceCommunicationFunctions
 */
public enum DeviceProtocolCapabilities {

    /**
     * Indication that a device can initiate its own protocol session but is not
     * necessarily also in control of the communication connection (e.g. when connected to a gateway)
     */
    PROTOCOL_SESSION(DeviceCommunicationFunction.PROTOCOL_SESSION),
    /**
     * Indicates that this device is able to server as a <i>Master</i> for logical slave devices
     */
    PROTOCOL_MASTER(DeviceCommunicationFunction.PROTOCOL_MASTER),
    /**
     * Indicates that this device is not able to create its own protocol session, but relies on his
     * <i>Master</i> to read/contact the physical device.
     */
    PROTOCOL_SLAVE(DeviceCommunicationFunction.PROTOCOL_SLAVE);

    /**
     * The corresponding {@link DeviceCommunicationFunction}
     */
    private final DeviceCommunicationFunction deviceFunction;

    DeviceProtocolCapabilities(DeviceCommunicationFunction protocolSession) {
        deviceFunction = protocolSession;
    }

    /**
     * Provides the corresponding {@link DeviceCommunicationFunction}
     *
     * @return the corresponding DeviceCommunicationFunction
     */
    public DeviceCommunicationFunction getDeviceFunction() {
        return deviceFunction;
    }

    /**
     * Bit0: slave capability
     * Bit1: session capability
     * Bit2: master capability
     */
    public static List<DeviceProtocolCapabilities> fromFlags(Integer flags) {
        if (flags == null) {   //Default, if there's no capabilities mapped
            return Collections.singletonList(DeviceProtocolCapabilities.PROTOCOL_SESSION);
        }

        List<DeviceProtocolCapabilities> result = new ArrayList<>();
        if ((flags & 0x01) == 0x01) {
            result.add(DeviceProtocolCapabilities.PROTOCOL_SLAVE);
        }
        if ((flags & 0x02) == 0x02) {
            result.add(DeviceProtocolCapabilities.PROTOCOL_SESSION);
        }
        if ((flags & 0x04) == 0x04) {
            result.add(DeviceProtocolCapabilities.PROTOCOL_MASTER);
        }
        return result;
    }
}