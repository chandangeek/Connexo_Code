/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointLog;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;

public class EndPointLogInfoFactory {
    private final Thesaurus thesaurus;
    private final EndPointConfigurationInfoFactory endPointConfigurationInfoFactory;
    private final WebServiceCallOccurrenceInfoFactory webServiceCallOccurrenceInfoFactory;

    @Inject
    public EndPointLogInfoFactory(Thesaurus thesaurus,
                                  EndPointConfigurationInfoFactory endPointConfigurationInfoFactory,
                                  WebServiceCallOccurrenceInfoFactory webServiceCallOccurrenceInfoFactory) {
        this.thesaurus = thesaurus;
        this.endPointConfigurationInfoFactory = endPointConfigurationInfoFactory;
        this.webServiceCallOccurrenceInfoFactory = webServiceCallOccurrenceInfoFactory;
    }

    public EndPointLogInfo from(EndPointLog endPointLog) {
        EndPointLogInfo info = new EndPointLogInfo();
        info.id = endPointLog.getId();
        info.logLevel = thesaurus.getString(endPointLog.getLogLevel().getKey(), endPointLog.getLogLevel()
                .getDefaultFormat());
        info.message = endPointLog.getMessage();
        info.timestamp = endPointLog.getTime();
        return info;
    }

    public EndPointLogInfo fullInfoFrom(EndPointLog endPointLog, UriInfo uriInfo) {
        EndPointLogInfo info = from(endPointLog);
        info.endPointConfigurationInfo = endPointConfigurationInfoFactory.from(endPointLog.getEndPointConfiguration(), uriInfo);
        info.stackTrace = endPointLog.getStackTrace();
        info.occurrenceInfo = webServiceCallOccurrenceInfoFactory.from(endPointLog.getOccurrence().get(), uriInfo, false);
        return info;
    }
}
