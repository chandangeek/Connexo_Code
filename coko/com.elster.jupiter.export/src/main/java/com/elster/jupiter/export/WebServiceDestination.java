/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface WebServiceDestination extends DataExportDestination {

    String TYPE_IDENTIFIER = "WEBSD";

    EndPointConfiguration getCreateWebServiceEndpoint();

    void setCreateWebServiceEndpoint(EndPointConfiguration endPointConfiguration);

    Optional<EndPointConfiguration> getChangeWebServiceEndpoint();

    void setChangeWebServiceEndpoint(EndPointConfiguration endPointConfiguration);
}
