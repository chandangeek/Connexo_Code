/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.edmi.mk10.packets;

/**
 * This enum describes the possible EDMI MK10 push packet types
 * @author jme
 * @since 15-10-2009
 */
public enum PushPacketType {

	README, ALARM, UPS_HEARTBEAT, COMMISSIONING, HEARTBEAT, UNKNOWN;

	private static final int	ADDRESS_README			= 0x0000FFE0;
	private static final int	ADDRESS_ALARM			= 0x0000FFE1;
	private static final int	ADDRESS_UPS_HEARTBEAT	= 0x0000FFE2;
	private static final int	ADDRESS_COMMISSIONING	= 0x0000FFE3;
	private static final int	ADDRESS_HEARTBEAT1		= 0x0000FFE4;
	private static final int	ADDRESS_HEARTBEAT2		= 0x0000FFE5;

	private static final int	INTEGER_MASK			= 0x0FFFF;

	/**
	 * Get a human readable description of the packet type (ex: "Read-me packet", ...)
	 * @return A string containing the description
	 */
	public String getDescription() {
		switch (this) {
		case README:
			return "Read-me packet";
		case ALARM:
			return "Alarm packet";
		case UPS_HEARTBEAT:
			return "UPS heartbeat packet";
		case COMMISSIONING:
			return "Commissioning packet";
		case HEARTBEAT:
			return "Heartbeat packet";
		case UNKNOWN:
			return "Unknown packet";
		default:
			return "Unknown packet";
		}
	}

	/**
	 * Get the correct {@link PushPacketType} matching with the given address.
	 * When the addres is invalid or unknown, {@link PushPacketType.UNKNOWN} is returned
	 * @param address The address of the packettype
	 * @return the {@link PushPacketType}
	 */
	public static PushPacketType getPacketType(int address) {
		switch (address & INTEGER_MASK) {
		case ADDRESS_README:
			return README;
		case ADDRESS_ALARM:
			return ALARM;
		case ADDRESS_UPS_HEARTBEAT:
			return UPS_HEARTBEAT;
		case ADDRESS_COMMISSIONING:
			return COMMISSIONING;
		case ADDRESS_HEARTBEAT1:
		case ADDRESS_HEARTBEAT2:
			return HEARTBEAT;
		default:
			return UNKNOWN;
		}
	}

}
