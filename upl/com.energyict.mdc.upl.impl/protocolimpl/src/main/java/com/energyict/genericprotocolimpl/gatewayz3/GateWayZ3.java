package com.energyict.genericprotocolimpl.gatewayz3;

/**
 * <p>
 * Implements the GateWay Z3 protocol. The Z3 will act as a Master/Gateway in an RF-Mesh
 * network with a certain amount of R2 slave devices.
 * After fetching the "routingTable" of the Z3, the protocol will handle each R2 one by one.
 * The handling of the R2's will depend on the nextCommunicationDate of his CommunicationProtocol.
 * </p><p>
 * <u>NOTE:</u>
 * The communication to a slave should be started with a postDialCommand:
 * <b>&lt;ESC&gt;rfclient="rfclientid"&lt;/ESC&gt;</b>
 * Normally you should use an 'IPDialerSelector' for this, but because we use the
 * same link from the Z3, we should send it our selves
 * </p>
 *
 * @author gna
 * @since 21 October 2009
 * @deprecated generic protocols are no longer supported, a proper DeviceProtocol has been created to replace this
 */
public class GateWayZ3 {}
