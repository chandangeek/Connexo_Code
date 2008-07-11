package com.energyict.protocolimpl.elster.opus;

public class OpusCommunicationStateMachine {
	/*
	 * All commands have a similar state machine
	 * this class will try to deal with the full command machine
	 */
	private static final char SOH =0x0001;  // start of heading
	private static final char STX =0x0002;  // start of text
	private static final char ETX =0x0003;  // end of text
	private static final char EOT =0x0004;  // end of transmission 
	private static final char ENQ =0x0005;  // enquiry
	private static final char ACK =0x0006;  // acknowledge
	private static final char CR  =0x000D;  // carriage return
	private static final char XON =0x0011;  // instruction packet control characters
	private static final char XOFF=0x0013;  // instruction packet control characters
	private static final char NAK =0x0021;  // negative acknowledge

}
