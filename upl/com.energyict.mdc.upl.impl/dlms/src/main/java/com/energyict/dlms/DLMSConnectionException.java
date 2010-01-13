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

	private static final int UNKNOWN_REASON = -1;
	private short sReason;

	public DLMSConnectionException(String str) {
		super(str);
		this.sReason = UNKNOWN_REASON;
	}

	public DLMSConnectionException() {
		super();
		this.sReason = UNKNOWN_REASON;
	}

	public DLMSConnectionException(String str, short sReason) {
		super(str);
		this.sReason = sReason;
	}

	public DLMSConnectionException(Throwable ex) {
		super(ex);
		this.sReason = UNKNOWN_REASON;
	}

	public short getReason() {
		return sReason;
	}

}