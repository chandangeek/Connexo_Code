package com.energyict.mdc.protocol.api;

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
 * Copyrights EnergyICT
 * Date: 26/11/12
 * Time: 15:31
 */
public enum DeviceProtocolCapabilities {

    /**
     * Indication that a device can initiate its own protocol session but is not
     * necessarily also in control of the communication connection (e.g. when connected to a gateway)
     */
    PROTOCOL_SESSION,
    /**
     * Indicates that this device is able to server as a <i>Master</i> for logical slave devices
     */
    PROTOCOL_MASTER,
    /**
     * Indicates that this device is not able to create its own protocol session, but relies on his
     * <i>Master</i> to read/contact the physical device.
     */
    PROTOCOL_SLAVE;

}