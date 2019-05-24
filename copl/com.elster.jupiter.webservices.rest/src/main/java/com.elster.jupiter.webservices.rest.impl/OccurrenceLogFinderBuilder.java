package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointLog;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface OccurrenceLogFinderBuilder {

    public OccurrenceLogFinderBuilder withOccurrenceId(long occurrenceId);

    public Finder<EndPointLog> build();

}
