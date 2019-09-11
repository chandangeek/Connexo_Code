/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointLog;

import javax.inject.Inject;

public class EndPointLogEntryInfoFactory {
    private final Thesaurus thesaurus;

    @Inject
    EndPointLogEntryInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    EndPointLogEntryInfo from(EndPointLog entry) {
        EndPointLogEntryInfo info = new EndPointLogEntryInfo();
        info.timestamp = entry.getTime();
        info.message = entry.getMessage();
        info.logLevel = new IdWithNameInfo(entry.getLogLevel().name(), entry.getLogLevel().getDisplayName(thesaurus));
        return info;
    }

}
