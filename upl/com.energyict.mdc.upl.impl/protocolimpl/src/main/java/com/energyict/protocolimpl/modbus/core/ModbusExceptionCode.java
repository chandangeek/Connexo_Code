package com.energyict.protocolimpl.modbus.core;

/**
 * Enumerator containing the different Modbus exception codes
 *
 * @author sva
 * @since 20/11/13 - 14:25
 */
public enum ModbusExceptionCode {

    /**
     * The Function Code received in the query is not an allowable action for the server (or slave). This may be because the
     * function code is only applicable to newer devices, and was not implemented in the unit selected. It could also indicate
     * that the server (or slave) is in the wrong state to process a request of this type, for example because it is not configured
     * and is being asked to return register values.
     */
    ILLEGAL_FUNCTION(1, "Illegal function", false),

    /**The data address received in the query is not an allowable address for the server (or slave). More specifically, the
     * combination of reference number and transfer length is invalid. For a controller with 100 registers a request of offset
     * 96 and a length of 5 will generate exception 02.
     **/
    ILLEGAL_DATA_ADDRESS(2, "Illegal data address", false),

    /**
     * The value contained in the query data field is not an allowable value for the server (or slave). This indicates a fault
     * in the structure of the remainder of a complex request, such as that the implied length is incorrect. It specifically
     * does NOT mean that a data item submitted for storage in a register has a value outside the expectation of the application program,
     * since the MODBUS protocol is unaware of the significance of any particular value of any particular register.
     */
    ILLEGAL_DATA_VALUE(3, "Illegal data value", false),

    /**
     * An Unrecoverable error occurred while the server (or slave) was attempting to perform the requested action.
     */
    FAILURE_IN_ASSOCIATED_DEVICE(4, "Failure in associated device", false),

    /**
     * Specialized in conjunction with programming commands. The server (or slave) has accepted the request and is
     * processing it, but long duration of time will be required to do so. This response is returned to prevent a timeout
     * error from occurring in the client (or master). The client (or master) can next issue a poll program complete message
     * to determine if processing is completed.
     */
    ACK(5, "Acknowledge - device is processing the request", false),

    /**
     * Specialized use in conjunction with programming commands. The server (of slave) is engaged in processing a long-duration
     * program command. The client (or master) should retransmit the message later when the server (or slave) is free.
     **/
    REJECTED_CAUSE_BUSY(6, "Rejected - the associated device is busy and cannot accept any new requests", true),

    /**
     * The program function just requested cannot b performed. Issue poll to obtain detailed device dependent error
     * information. Valid for Program/Poll 13 and 14 only.
     */
    NACK(7, "Negative acknowledgement - request cannot be performed", false),

    /**
     * Specialized use in conjunction with function codes 20 and 21 and reference type 6, to indicate that
     * the extended file area failed to pass a consistency check. The server (or slave) attempted to read record
     * file, but detected a parity error in the memory. The client (or master) can retry the request, but service
     * may be required on the server (or slave) device.
     */
    MEMORY_PARITY_ERROR(8, "Memory parity error", true);

    /**
     * The integer exception code returned by the device.
     */
    private final int exceptionCode;

    /**
     * The description of the error.
     */
    private final String description;

    /**
     * Boolean indicating if the exception is persistent (we should not retry, cause we will bump into the same error again)
     *  or if it is only temporary failure (for which retries are allowed)
     */
    private final boolean retryAllowed;

    private ModbusExceptionCode(int exceptionCode, String description, boolean retryAllowed) {
        this.exceptionCode = exceptionCode;
        this.description = description;
        this.retryAllowed = retryAllowed;
    }

    /**
     * Getter for the exception code
     */
    public int getExceptionCode() {
        return exceptionCode;
    }

    /**
     * Getter for the error description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Indication whether it is allowed to retry the failed request or not
     * @return  true, if the failed request can be retried
     *          false, if retries are not allowed (cause we gonna bump into the same error)
     */
    public boolean isRetryAllowed() {
        return retryAllowed;
    }

    /**
     * Returns the corresponding {ModbusExceptionCode}.
     *
     * @param exceptionCode The exception code.
     * @return The corresponding {@link ModbusExceptionCode}.
     */
    public static ModbusExceptionCode byResultCode(final int exceptionCode) {
        for (final ModbusExceptionCode code : values()) {
            if (code.getExceptionCode() == exceptionCode) {
                return code;
            }
        }
        return null;
    }
}
