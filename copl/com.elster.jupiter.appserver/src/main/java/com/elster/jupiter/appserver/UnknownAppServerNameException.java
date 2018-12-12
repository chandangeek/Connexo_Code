/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class UnknownAppServerNameException extends LocalizedException {

	private static final long serialVersionUID = 1L;

	public UnknownAppServerNameException(String appServerName, Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.APPSERVER_NAME_UNKNOWN, appServerName);
        set("appServerName", appServerName);
    }

}
