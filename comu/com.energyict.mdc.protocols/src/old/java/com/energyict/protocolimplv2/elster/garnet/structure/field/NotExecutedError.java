package com.energyict.protocolimplv2.elster.garnet.structure.field;

import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

/**
 * @author sva
 * @since 23/05/2014 - 10:05
 */
public class NotExecutedError extends AbstractField<NotExecutedError> {

    public static final int LENGTH = 1;

    private int errorCodeId;
    private ErrorCode errorCode;

    public NotExecutedError() {
        this.errorCode = ErrorCode.UNKNOWN;
    }

    public NotExecutedError(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromIntLE(errorCode.getErrorCode(), LENGTH);
    }

    @Override
    public NotExecutedError parse(byte[] rawData, int offset) throws ParsingException {
        errorCodeId = getIntFromBytesLE(rawData, offset, LENGTH);
        errorCode = ErrorCode.fromCode(errorCodeId);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public int getErrorCodeId() {
        return errorCodeId;
    }

    public String getErrorCodeInfo() {
        if (!this.errorCode.equals(ErrorCode.UNKNOWN)) {
            return errorCode.getErrorDescription();
        } else {
            return (errorCode.getErrorDescription() + " " + errorCodeId);
        }
    }

    public enum ErrorCode {

        COMMAND_NOT_IMPLEMENTED(0x00, "Command not implemented"),
        CRC_ERROR(0x01, "CRC error"),
        MAJOR_DATA(0x02, "Date and time of the command not synchronized to internal clock - time difference exceeds + 5min"),
        MINOR_DATA(0x03, "Date and time of the command not synchronized to internal clock - time difference exceeds - 5min"),
        SLAVE_DOES_NOT_EXIST(0x04, "Slave does not exist"),
        UNKNOWN(0x0, "Unknown error");

        /** The error code of the error **/
        private final int errorCode;

        /** The textual description of the error **/
        private final String errorDescription;


        private ErrorCode(int errorCode, String errorDescription) {
            this.errorCode = errorCode;
            this.errorDescription = errorDescription;
        }

        public int getErrorCode() {
            return errorCode;
        }

        public String getErrorDescription() {
            return errorDescription;
        }

        public static ErrorCode fromCode(int code) {
            for (ErrorCode errorCode : ErrorCode.values()) {
                if (errorCode.getErrorCode() == code) {
                    return errorCode;
                }
            }
            return ErrorCode.UNKNOWN;
        }
    }
}