/*
 * ProtocolConnection.java
 *
 * Created on 26 juli 2004, 14:03
 */

package com.energyict.protocolimpl.base;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.protocol.api.inbound.MeterType;

import java.io.IOException;

/**
 * The low level communication class extending from Connection should also implement ProtocolConnection interface. That interface is returned by the doInit(...) abstract method implemented in the protocol implementation class that extends AbstractProtocol.
 * @author Koen
 */
public interface ProtocolConnection {

	/**
	 * Setter for the HHU (hand held unit) specific methods. No implementation in most cases.
	 * @param hhuSignOn hhuSignOn
	 */
	void setHHUSignOn(HHUSignOn hhuSignOn);
	/**
	 * Getter for the HHU (hand held unit) specific methods. No implementation in most cases.
	 * @return hhuSignOn
	 */
	HHUSignOn getHhuSignOn();
	/**
	 * Implements the specific meter communication disconnect
	 * @throws NestedIOException Thrown when something goes wrong different from a protocol related exception
	 * @throws com.energyict.protocolimpl.base.ProtocolConnectionException thrown for protocol and communication related exceptions
	 */
	void disconnectMAC() throws NestedIOException, ProtocolConnectionException;
	/**
	 * Implements the specific meter communication connect
	 * @param strID property MeterProtocol.ADDRESS (DeviceId)
	 * @param strPassword property MetrProtocol.PASSWORD (Password)
	 * @param securityLevel custom property "SecurityLevel"
	 * @param nodeId property MeterProtocol.NODEID (NodeAddress)
	 * @throws java.io.IOException Thrown when something goes wrong different from a protocol related exception
	 * @throws com.energyict.protocolimpl.base.ProtocolConnectionException thrown for protocol and communication related exceptions
	 * @return MeterType is a class with meter specific info. Implemented specific for the IEC1107 optical head communication and signon related info. However, MeterType can be used in other implementations too.
	 */
	MeterType connectMAC(String strID,String strPassword,int securityLevel,String nodeId) throws IOException, ProtocolConnectionException;
	/**
	 * Implements the dataReadout functionality. Specific IEC1107 protocol related.
	 * @param strID property MeterProtocol.ADDRESS (DeviceId)
	 * @param nodeId property MeterProtocol.NODEID (NodeAddress)
	 * @throws NestedIOException Thrown when something goes wrong different from a protocol related exception
	 * @throws com.energyict.protocolimpl.base.ProtocolConnectionException thrown for protocol and communication related exceptions
	 * @return byte[] data
	 */
	byte[] dataReadout(String strID,String nodeId) throws NestedIOException, ProtocolConnectionException;
}
