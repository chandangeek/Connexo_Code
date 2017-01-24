package com.energyict.dlms.cosem;

/**
 * Enumerates the data access result codes.
 *
 * @author jme
 */
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
    LONG_GET_ABORTED(15, "Long get aborted"),
    NO_LONG_GET_IN_PROGRESS(16, "No long get in progress"),
    LONG_SET_ABORTED(17, "Long set aborted"),
    NO_LONG_SET_IN_PROGRESS(18, "No long set in progress"),
    DATA_BLOCK_NUMBER_INVALID(19, "Data block number in valid"),
    OTHER(250, "Other reason");

    /**
     * This is the integer result code returned by the device.
     */
    private final int result;

    /**
     * The description of the error.
     */
    private final String description;

    /**
     * Create a new instance using the result and description.
     *
     * @param result      The result.
     * @param description The description.
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
     * @param resultCode The result code.
     * @return The corresponding {@link DataAccessResultCode}.
     */
    public static DataAccessResultCode byResultCode(final int resultCode) {
        for (final DataAccessResultCode code : values()) {
            if (code.result == resultCode) {
                return code;
            }
        }
        return null;
    }
}
