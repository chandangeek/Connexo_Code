/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.util.IdWithLocalizedValueInfo;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceProtocol;

import java.util.List;

public class WebServicesInfo {
    public String name;
    public IdWithLocalizedValueInfo<WebServiceDirection> direction;
    public WebServiceProtocol type;
    public List<PropertyInfo> properties;
}
