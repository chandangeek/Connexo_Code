package com.energyict.dlms.cosem;

import java.io.IOException;

/**
 *
 * @author kvds
 */
public final class DataAccessResultException extends IOException {

	/** Required for serializable classes. */
	private static final long serialVersionUID = 1L;

	/** Enumerates the most frequent data access result codes. */
	public enum DataAccessResultCode {
		SUCCESS(0, "Success"),
		HARDWARE_FAULT(1, "Hardware fault"),
		TEMPORARY_FAILURE(2, "Temporary failure"),
		RW_DENIED(3, "R/W denied"),
		OBJECT_UNDEFINED(4, "Object undefined"),
		OBJECTCLASS_INCONSISTENT(9, "Object class inconsistent"),
		OBJECT_UNAVAILABLE(11, "Object unavailable"),
		TYPE_UNMATCHED(12, "Type unmatched"),
		ACCESS_SCOPE_VIOLATION(13, "Scope of access violation"),
		DATA_BLOCK_UNAVAILABLE(14, "Data block unavailable"),
		OTHER(255, "Other reason");

		/** This is the integer result code returned by the device. */
		private final int result;

		/** The description of the error. */
		private final String description;

		/**
		 * Create a new instance using the result and description.
		 * 
		 * @param result
		 *            The result.
		 * @param rescription
		 *            The description.
		 */
		private DataAccessResultCode(final int result, final String description) {
			this.result = result;
			this.description = description;
		}

		/**
		 * Returns the result code as it was returned by the device.
		 * 
		 * @return The result code as it was returned by the device.
		 */
		public int getResultCode() {
			return this.result;
		}

		/**
		 * Returns the description.
		 * 
		 * @return The description.
		 */
		public String getDescription() {
			return this.description;
		}

		/**
		 * Returns the corresponding data access result code.
		 * 
		 * @param resultCode
		 *            The result code.
		 * 
		 * @return The corresponding {@link DataAccessResultCode}.
		 */
		private static DataAccessResultCode byResultCode(final int resultCode) {
			for (final DataAccessResultCode code : values()) {
				if (code.result == resultCode) {
					return code;
				}
			}

			return null;
		}
	}

	/** The code as it was returned by the device. This is always filled in. */
	private final int dataAccessResult;

	/** This is a "parsed" version of the preceding field. */
	private final DataAccessResultCode dataAccessResultCode;

	/** Creates a new instance of DataAccessResultException */
	public DataAccessResultException(int dataAccessResult) {
		this(dataAccessResult, "Cosem Data-Access-Result exception "
				+ evalDataAccessResult(dataAccessResult));
	}

	public DataAccessResultException(int dataAccessResult, String message) {
		super(message);

		this.dataAccessResult = dataAccessResult;
		this.dataAccessResultCode = DataAccessResultCode
				.byResultCode(dataAccessResult);
	}

	public String toString() {
		return super.toString() + ", "
				+ evalDataAccessResult(getDataAccessResult());
	}

	public boolean isEvalDataAccessResultStandard() {
		return (dataAccessResult <= 14) || (dataAccessResult == 250);

	}

	public static String evalDataAccessResult(int val) {
		if (DataAccessResultCode.byResultCode(val) != null) {
			return DataAccessResultCode.byResultCode(val).getDescription();
		} else {
			return "Unknown data-access-result code " + val;
		}
	}

	public int getDataAccessResult() {
		return dataAccessResult;
	}

	/**
	 * Returns the data access result code if there is one.
	 * 
	 * @return The data access result code if there is one.
	 */
	public DataAccessResultCode getCode() {
		return this.dataAccessResultCode;
	}
}
