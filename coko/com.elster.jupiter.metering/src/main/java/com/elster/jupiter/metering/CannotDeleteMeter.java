/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class CannotDeleteMeter extends LocalizedException {
	
	private static final long serialVersionUID = 1L;

	public CannotDeleteMeter(Thesaurus thesaurus, MessageSeed reason, String mRID) {
        super(thesaurus, reason, mRID);
    }
}
