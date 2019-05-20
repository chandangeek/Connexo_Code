package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointLog;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointOccurrence;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;

public class EndpointConfigurationOccurrenceInfoFactorty {
    private final Thesaurus thesaurus;
    EndPointConfigurationInfoFactory endPointConfigurationInfoFactory;

    @Inject
    public EndpointConfigurationOccurrenceInfoFactorty(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public EndpointConfigurationOccurrenceInfo from(EndPointOccurrence endPointOccurrence, UriInfo uriInfo) {
        EndpointConfigurationOccurrenceInfo info = new EndpointConfigurationOccurrenceInfo();
        /*info.logLevel = thesaurus.getString(endPointLog.getLogLevel().getKey(), endPointLog.getLogLevel()
                .getDefaultFormat());*/
        info.startTime = endPointOccurrence.getStartTime();
        info.endTime = endPointOccurrence.getEndTime();
        info.status = endPointOccurrence.getStatus();
        if (uriInfo != null)
        info.endPointConfigurationInfo = endPointConfigurationInfoFactory.from(endPointOccurrence.getEndPointConfiguration(), uriInfo);


        return info;
    }


}
