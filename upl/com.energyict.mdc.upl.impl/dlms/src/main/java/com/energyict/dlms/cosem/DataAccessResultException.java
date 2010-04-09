package com.energyict.dlms.cosem;

import java.io.IOException;

/**
 * @author kvds
 */
public final class DataAccessResultException extends IOException {

	/** Required for serializable classes. */
	private static final long serialVersionUID	= 1L;

	/** The code as it was returned by the device. This is always filled in. */
	private final int dataAccessResult;

	/** This is a "parsed" version of the preceding field. */
	private final DataAccessResultCode dataAccessResultCode;

	/** Creates a new instance of DataAccessResultException */
	public DataAccessResultException(int dataAccessResult) {
		this(dataAccessResult, "Cosem Data-Access-Result exception " + evalDataAccessResult(dataAccessResult));
	}

	public DataAccessResultException(int dataAccessResult, String message) {
		super(message);
		this.dataAccessResult = dataAccessResult;
		this.dataAccessResultCode = DataAccessResultCode.byResultCode(dataAccessResult);
	}

	/**
	 * @return
	 */
	public boolean isEvalDataAccessResultStandard() {
		return (dataAccessResult <= 14) || (dataAccessResult == 250);
	}

	/**
	 * @param val
	 * @return
	 */
	public static String evalDataAccessResult(int val) {
		if (DataAccessResultCode.byResultCode(val) != null) {
			return DataAccessResultCode.byResultCode(val).getDescription();
		} else {
			return "Unknown data-access-result code " + val;
		}
	}

	/**
	 * Getter for the dataAccessResult as it was returned by the device. This is
	 * always filled in.
	 * @return the dataAccessResult as int
	 */
	public int getDataAccessResult() {
		return dataAccessResult;
	}

	/**
	 * Returns the data access result code if there is one.
	 * @return The data access result code if there is one.
	 */
	public DataAccessResultCode getCode() {
		return this.dataAccessResultCode;
	}

	public String toString() {
		return super.toString() + ", " + evalDataAccessResult(getDataAccessResult());
	}

}
