package com.elster.jupiter.metering;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.BaseException;

public class IllegalMRIDFormatException extends BaseException {
	
	private static final long serialVersionUID = 1L;

	public IllegalMRIDFormatException(String wrongMRID, Thesaurus thesaurus) {
        super(ExceptionTypes.ILLEGAL_MRID_FORMAT, buildMessage(wrongMRID, thesaurus));
        set("wrong MRID", wrongMRID);
    }

    public IllegalMRIDFormatException(String wrongMRID, Throwable cause, Thesaurus thesaurus) {
        super(ExceptionTypes.ILLEGAL_MRID_FORMAT, buildMessage(wrongMRID, thesaurus), cause);
        set("wrong MRID", wrongMRID);
    }

    private static String buildMessage(String wrongMRID, Thesaurus thesaurus) {
        return thesaurus.getFormat(MessageSeeds.ILLEGAL_MRID_FORMAT).format(wrongMRID);
    }

}
