package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.domain.util.Finder;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface OccurrenceLogFinderBuilder {

    public OccurrenceLogFinderBuilder withOccurrenceId(WebServiceCallOccurrence epoc);

    public Finder<EndPointLog> build();

}

