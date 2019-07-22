package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;

public class WebServiceCallOccurrenceInfoFactory {
    private final Thesaurus thesaurus;
    EndPointConfigurationInfoFactory endPointConfigurationInfoFactory;

    @Inject
    public WebServiceCallOccurrenceInfoFactory(Thesaurus thesaurus,
                                               EndPointConfigurationInfoFactory endPointConfigurationInfoFactory) {
        this.thesaurus = thesaurus;
        this.endPointConfigurationInfoFactory = endPointConfigurationInfoFactory;
    }

    public WebServiceCallOccurrenceInfo from(WebServiceCallOccurrence endPointOccurrence, UriInfo uriInfo, Boolean withPayload) {
        WebServiceCallOccurrenceInfo info = new WebServiceCallOccurrenceInfo();

        info.id = endPointOccurrence.getId();
        info.startTime = endPointOccurrence.getStartTime();
        info.status = endPointOccurrence.getStatus().getDisplayName(thesaurus);
        endPointOccurrence.getEndTime().ifPresent(endTime -> info.endTime = endTime);
        endPointOccurrence.getRequest().ifPresent(request -> info.request = request);
        endPointOccurrence.getApplicationName().ifPresent(applicationName -> info.applicationName = applicationName);

        if (withPayload){
            endPointOccurrence.getPayload().ifPresent(payload -> info.payload = payload);
        }
        if (uriInfo != null && endPointOccurrence.getEndPointConfiguration() != null) {
            info.endPointConfigurationInfo = endPointConfigurationInfoFactory.from(endPointOccurrence.getEndPointConfiguration(), uriInfo);
        }
        return info;
    }
}
