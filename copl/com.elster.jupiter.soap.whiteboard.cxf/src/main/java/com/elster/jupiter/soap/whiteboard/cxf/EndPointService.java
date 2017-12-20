/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.soap.whiteboard.cxf.impl.EndPointFactory;

import java.util.List;
import java.util.Map;

public interface EndPointService {
    String COMPONENT_NAME = "EPS";

    Map<String, EndPointFactory> getWebServices();

    void addWebService(String name, EndPointFactory factory);

    EndPointFactory getEndPointFactory(String serviceName);

    EndPointFactory removeWebService(String name);

    List<PropertySpec> getWebServicePropertySpecs(String webServiceName);
}
