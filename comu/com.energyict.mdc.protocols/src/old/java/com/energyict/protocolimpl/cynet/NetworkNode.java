/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.cynet;

/**
 * A network node is included in the network topology.
 * 
 * @author alex
 */
public final class NetworkNode {

	/** The manufacturer ID. */
	private final ManufacturerId manufacturerId;
	
	/** The signal strength. */
	private SignalStrength signalStrength = SignalStrength.UNKNOWN;
	
	/** This is the reverse reference to the network the node is included in. */
	private final Network network;
	
	/**
	 * Create a new instance.
	 * 
	 * @param 	manufacturerId		The manufacturer ID.
	 * @param	network				The parent network.
	 */
	NetworkNode(final ManufacturerId manufacturerId, final Network network) {
		this.manufacturerId = manufacturerId;
		this.network = network;
	}
	
	/**
	 * Returns the manufacturer ID.
	 * 
	 * @return	The manufacturer ID.
	 */	
	public final ManufacturerId getManufacturerID() {
		return this.manufacturerId;
	}
	
	/**
	 * Returns the signal strength.
	 * 
	 * @return	The signal strength.
	 */
	public final SignalStrength getSignalStrength() {
		return this.signalStrength;
	}
	
	/**
	 * Sets the sigjnal strength.
	 * 
	 * @param 	signalStrength		The new signal strength.
	 */
	final void setSignalStrength(final SignalStrength signalStrength) {
		this.signalStrength = signalStrength;
	}
	
	/**
	 * Returns the parent network this node is situated in.
	 * 
	 * @return	The parent network of the node.
	 */
	public final Network getParentNetwork() {
		return this.network;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean equals(final Object obj) {
		if (obj instanceof NetworkNode) {
			final NetworkNode otherNode = (NetworkNode)obj;
			
			if (otherNode.getManufacturerID() != null && this.manufacturerId != null) {
				return this.manufacturerId.equals(otherNode.getManufacturerID());
			}
		}
		
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int hashCode() {
		return this.manufacturerId.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String toString() {
		return new StringBuilder("Network node [").append(this.manufacturerId.toHexString()).append("], signal strength [" + this.signalStrength + "]").toString();
	}
	
}
