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
    public EndpointConfigurationOccurrenceInfoFactorty(Thesaurus thesaurus,
                                                       EndPointConfigurationInfoFactory endPointConfigurationInfoFactory)
    {
        this.thesaurus = thesaurus;
        this.endPointConfigurationInfoFactory = endPointConfigurationInfoFactory;
    }

    public EndpointConfigurationOccurrenceInfo from(EndPointOccurrence endPointOccurrence, UriInfo uriInfo) {
        EndpointConfigurationOccurrenceInfo info = new EndpointConfigurationOccurrenceInfo();
        /*info.logLevel = thesaurus.getString(endPointLog.getLogLevel().getKey(), endPointLog.getLogLevel()
                .getDefaultFormat());*/
        System.out.println("PREPARE INFO OBJECT FOR OCCURRENCE id ="+endPointOccurrence.getId());
        System.out.println("PAYLOAD = "+endPointOccurrence.getPayload());

        info.id = endPointOccurrence.getId();
        info.startTime = endPointOccurrence.getStartTime();
        info.endTime = endPointOccurrence.getEndTime();
        info.status = endPointOccurrence.getStatus();
        info.request = endPointOccurrence.getRequest();
        info.applicationName = endPointOccurrence.getApplicationName();
        if (uriInfo != null && endPointOccurrence.getEndPointConfiguration() != null){
            info.endPointConfigurationInfo = endPointConfigurationInfoFactory.from(endPointOccurrence.getEndPointConfiguration(), uriInfo);
        }

        if (endPointOccurrence.getPayload() != null){
            info.payload = endPointOccurrence.getPayload();
        }

        return info;
    }


}
