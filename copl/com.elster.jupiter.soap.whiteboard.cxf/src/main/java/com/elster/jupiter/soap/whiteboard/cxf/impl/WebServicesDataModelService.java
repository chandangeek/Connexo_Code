/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;

public interface WebServicesDataModelService {
    DataModel getDataModel();

    Thesaurus getThesaurus();

    WebServicesService getWebServicesService();

    EndPointConfigurationService getEndPointConfigurationService();
}
