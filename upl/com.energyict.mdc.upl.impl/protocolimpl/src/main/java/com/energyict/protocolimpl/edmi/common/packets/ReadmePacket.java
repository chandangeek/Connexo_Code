package com.energyict.protocolimpl.edmi.common.packets;

/**
 * 4 bytes: Serial number
 * 4 bytes: Time
 * 4 bytes: Load Survey 1 start time
 * 1 byte:  Number of channels in Load Survey 1
 * 1 byte:  Interval of Load Survey 1 in minutes
 * 1 byte:  UDP type. Always 0
 * 1 byte:  Number of billing resets.
 * 2 bytes: Latched EFAs
 * 
 * @author jme
 *
 */
public class ReadmePacket extends PushPacket {

	public static final String		FIELD_SERIAL					= "serial";
	public static final String		FIELD_TIME						= "time";
	public static final String		FIELD_START_TIME_LS1			= "loadSurvey1StartTime";
	public static final String		FIELD_NUMBER_OF_CHANNELS_LS1	= "loadSurvey1NumberOfChannels";
	public static final String		FIELD_INTERVAL_LS1				= "LoadSurvey1Interval";
	public static final String		FIELD_UDP_TYPE					= "udpType";
	public static final String		FIELD_NUMBER_OF_BILLING_RESETS	= "numberOfBillingResets";
	public static final String		FIELD_LATCHED_EFA				= "latchedEFA";

	private static final Object[][] PACKET_FIELD =	{
		{FIELD_PUSH_PACKET_TYPE, ""},
		{FIELD_SERIAL, ""},
		{FIELD_TIME, ""},
		{FIELD_START_TIME_LS1, ""},
		{FIELD_NUMBER_OF_CHANNELS_LS1, ""},
		{FIELD_INTERVAL_LS1, ""},
		{FIELD_UDP_TYPE, ""},
		{FIELD_NUMBER_OF_BILLING_RESETS, ""},
		{FIELD_LATCHED_EFA, ""},
	};

	public ReadmePacket(byte[] packetData) {
		super(packetData);
	}

	@Override
	void doParse() {
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ReadmePacket = ");
		builder.append(getClass().getName());
		builder.append(super.toString());
		return builder.toString();
	}
}