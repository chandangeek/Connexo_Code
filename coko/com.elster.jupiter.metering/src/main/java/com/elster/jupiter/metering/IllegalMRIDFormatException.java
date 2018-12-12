/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class IllegalMRIDFormatException extends LocalizedException {
	
	private static final long serialVersionUID = 1L;

	public IllegalMRIDFormatException(Thesaurus thesaurus, String wrongMRID) {
        super(thesaurus, MessageSeeds.ILLEGAL_MRID_FORMAT, wrongMRID);
        set("wrong MRID", wrongMRID);
    }

    public IllegalMRIDFormatException(String wrongMRID, Throwable cause, Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.ILLEGAL_MRID_FORMAT, cause, wrongMRID);
        set("wrong MRID", wrongMRID);
    }
}
