package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointLog;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointOccurrence;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface OccurrenceLogFinderBuilder {

    public OccurrenceLogFinderBuilder withOccurrenceId(EndPointOccurrence epoc);

    public Finder<EndPointLog> build();

}
