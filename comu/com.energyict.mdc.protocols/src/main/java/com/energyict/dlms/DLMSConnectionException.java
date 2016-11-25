package com.energyict.dlms;

/**
 * <p>Title: Meter Dialup package.</p>
 * <p>Description: Modem dialup and Energy Meter protocol implementation.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: EnergyICT</p>
 * @author Koenraad Vanderschaeve
 * @version 1.0
 */

public class DLMSConnectionException extends Exception {

    public static final int REASON_UNKNOWN = -1;

    /**
     * We received an invalid frame counter (smaller or equal to the previous one),
     * and should abort the communication session. (default behaviour)
     */
    public static final short REASON_ABORT_INVALID_FRAMECOUNTER = 1;

    /**
     * We received an invalid frame counter (smaller or equal to the previous one),
     * but should continue the communication session.
     * This means, disregard the received APDU and keep listening for a next APDU.
     * This implementation can be used for UDP (e.g. G3PLC) networks.
     */
    public static final short REASON_CONTINUE_INVALID_FRAMECOUNTER = 2;

	private short sReason;

	public DLMSConnectionException(String str) {
		super(str);
		this.sReason = REASON_UNKNOWN;
	}

	public DLMSConnectionException() {
		super();
		this.sReason = REASON_UNKNOWN;
	}

	public DLMSConnectionException(String str, short sReason) {
		super(str);
		this.sReason = sReason;
	}

	public DLMSConnectionException(Throwable ex) {
		super(ex);
		this.sReason = REASON_UNKNOWN;
	}

	public short getReason() {
		return sReason;
	}

}