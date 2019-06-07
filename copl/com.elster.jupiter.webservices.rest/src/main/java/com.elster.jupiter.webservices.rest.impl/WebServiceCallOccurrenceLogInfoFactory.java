/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointLog;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;

public class WebServiceCallOccurrenceLogInfoFactory {
    private final Thesaurus thesaurus;
    EndPointConfigurationInfoFactory endPointConfigurationInfoFactory;
    WebServiceCallOccurrenceInfoFactory webServiceCallOccurrenceInfoFactory;

    @Inject
    public WebServiceCallOccurrenceLogInfoFactory(Thesaurus thesaurus,
                                                  EndPointConfigurationInfoFactory endPointConfigurationInfoFactory,
                                                  WebServiceCallOccurrenceInfoFactory webServiceCallOccurrenceInfoFactory) {
        this.thesaurus = thesaurus;
        this.endPointConfigurationInfoFactory = endPointConfigurationInfoFactory;
        this.webServiceCallOccurrenceInfoFactory = webServiceCallOccurrenceInfoFactory;
    }

    public WebServiceCallOccurrenceLogInfo from(EndPointLog endPointLog) {
        WebServiceCallOccurrenceLogInfo info = new WebServiceCallOccurrenceLogInfo();
        info.id = endPointLog.getId();
        info.logLevel = thesaurus.getString(endPointLog.getLogLevel().getKey(), endPointLog.getLogLevel()
                .getDefaultFormat());
        info.message = endPointLog.getMessage();
        info.timestamp = endPointLog.getTime();
        return info;
    }

    public WebServiceCallOccurrenceLogInfo fromFull(EndPointLog endPointLog, UriInfo uriInfo) {
        WebServiceCallOccurrenceLogInfo info = new WebServiceCallOccurrenceLogInfo();
        info.logLevel = thesaurus.getString(endPointLog.getLogLevel().getKey(), endPointLog.getLogLevel()
                .getDefaultFormat());
        info.message = endPointLog.getMessage();
        info.timestamp = endPointLog.getTime();
        info.id = endPointLog.getId();
        info.endPointConfigurationInfo = endPointConfigurationInfoFactory.from(endPointLog.getEndPointConfiguration(), uriInfo);
        info.stackTrace = endPointLog.getStackTrace();
        info.occurrenceInfo = webServiceCallOccurrenceInfoFactory.from(endPointLog.getOccurrence().get(), uriInfo);
        return info;
    }
}
