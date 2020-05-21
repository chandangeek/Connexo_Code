/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface WebServicesDataModelService {
    DataModel getDataModel();

    Thesaurus getThesaurus();

    WebServicesService getWebServicesService();

    EndPointConfigurationService getEndPointConfigurationService();

    WebServiceCallOccurrenceService getWebServiceCallOccurrenceService();
}
