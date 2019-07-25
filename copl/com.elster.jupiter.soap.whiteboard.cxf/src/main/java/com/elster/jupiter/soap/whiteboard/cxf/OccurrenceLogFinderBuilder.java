package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.domain.util.Finder;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface OccurrenceLogFinderBuilder {

    OccurrenceLogFinderBuilder withEndPointConfiguration(EndPointConfiguration epc);

    OccurrenceLogFinderBuilder withNoOccurrence();

    OccurrenceLogFinderBuilder withOccurrenceId(WebServiceCallOccurrence epoc);

    Finder<EndPointLog> build();

}

