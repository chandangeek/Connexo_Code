package com.energyict.protocolimpl.cynet;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Signal strength listener. Keeps track of all the nodes in the network with their respective RSSI. The implementation listens for
 * ping packets as these contain the RSSI.
 * 
 * @author alex
 *
 */
final class SignalStrengthListener implements MessageListener {
	
	/** Contains the node IDs mapped onto their RSSI. */
	private final ConcurrentMap<ManufacturerId, SignalStrength> signalStrengths = new ConcurrentHashMap<ManufacturerId, SignalStrength>();

	/**
	 * {@inheritDoc}
	 */
	public final void messageReceived(final ManufacturerId source, final byte[] payload) {
		if (isPingMessage(payload)) {
			final int rssi = (payload[0] << 8) + (payload[1] & 0xFF);
			
			this.signalStrengths.put(source, SignalStrength.byRSSIValue(rssi));
		}
	}
	
	/**
	 * Check if the given message is a ping message.
	 * 
	 * @param 		message		The message.
	 * 
	 * @return		<code>true</code> if the message is a ping message, <code>false</code> if not.
	 */
	private static final boolean isPingMessage(final byte[] message) {
		return message != null && message.length == 4;
	}
	
	/**
	 * Returns the signal strength for the given node. Returns null if the node does not have a signal strength yet.
	 * 
	 * @param 		node		The node.
	 * 
	 * @return		the signal strength, {@link SignalStrength#UNKNOWN} if the node has not sent a ping packet yet.
	 */
	final SignalStrength getSignalStrength(final ManufacturerId node) {
		final SignalStrength rssi = this.signalStrengths.get(node);
		
		if (rssi == null) {
			return SignalStrength.UNKNOWN;
		}
		
		return rssi;
	}

}
