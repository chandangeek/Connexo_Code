/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointLog;

import javax.inject.Inject;

public class WebServiceCallOccurrenceLogInfoFactory {
    private final Thesaurus thesaurus;

    @Inject
    public WebServiceCallOccurrenceLogInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public WebServiceCallOccurrenceLogInfo from(EndPointLog endPointLog) {
        WebServiceCallOccurrenceLogInfo info = new WebServiceCallOccurrenceLogInfo();
        info.logLevel = thesaurus.getString(endPointLog.getLogLevel().getKey(), endPointLog.getLogLevel()
                .getDefaultFormat());
        info.message = endPointLog.getMessage();
        info.timestamp = endPointLog.getTime();
        return info;
    }

    public WebServiceCallOccurrenceLogInfo fromFull(EndPointLog endPointLog) {
        WebServiceCallOccurrenceLogInfo info = new WebServiceCallOccurrenceLogInfo();
        info.logLevel = thesaurus.getString(endPointLog.getLogLevel().getKey(), endPointLog.getLogLevel()
                .getDefaultFormat());
        info.message = endPointLog.getMessage();
        info.timestamp = endPointLog.getTime();
        info.id = endPointLog.getId();
        info.endPointConfiguration = endPointLog.getEndPointConfiguration();
        info.message = endPointLog.getMessage();
        info.stackTrace = endPointLog.getStackTrace();
        info.occurrence = endPointLog.getOccurrence().get();
        return info;
    }
}
