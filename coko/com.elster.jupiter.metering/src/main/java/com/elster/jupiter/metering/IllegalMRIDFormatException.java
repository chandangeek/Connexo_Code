package com.elster.jupiter.metering;

import com.elster.jupiter.util.exception.BaseException;

import java.text.MessageFormat;

public class IllegalMRIDFormatException extends BaseException {

    public IllegalMRIDFormatException(String wrongMRID) {
        super(ExceptionTypes.ILLEGAL_MRID_FORMAT, MessageFormat.format("Supplied MRID ''{0}'' is not the correct format.", wrongMRID));
        set("wrong MRID", wrongMRID);
    }

    public IllegalMRIDFormatException(String wrongMRID, Throwable cause) {
        super(ExceptionTypes.ILLEGAL_MRID_FORMAT, MessageFormat.format("Supplied MRID ''{0}'' is not the correct format.", wrongMRID), cause);
        set("wrong MRID", wrongMRID);
    }
}
