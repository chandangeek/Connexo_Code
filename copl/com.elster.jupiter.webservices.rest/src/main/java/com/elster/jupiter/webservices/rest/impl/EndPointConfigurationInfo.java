/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.util.IdWithLocalizedValueInfo;
import com.elster.jupiter.rest.util.LongIdWithNameInfo;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.soap.whiteboard.cxf.PayloadSaveStrategy;

import java.util.List;

/**
 * Model the configuration to create an actual endpoint. The latter is a purely runtime entity.
 */
public class EndPointConfigurationInfo {
    public long id;
    public long version;
    public String name;
    public String webServiceName;
    public String url;
    public String previewUrl;
    public IdWithLocalizedValueInfo<String> logLevel;
    public Boolean tracing;
    public String traceFile;
    public Boolean httpCompression;
    public Boolean schemaValidation;
    public Boolean active;
    public Boolean available;
    public IdWithLocalizedValueInfo<EndPointAuthentication> authenticationMethod;
    public LongIdWithNameInfo group;
    public String username;
    public String password; // TODO: Create a better way for storing sensetive data
    public String clientId;
    public String clientSecret; // TODO: Create a better way for storing sensetive data
    public IdWithLocalizedValueInfo<WebServiceDirection> direction;
    public String type;
    public List<PropertyInfo> properties;
    public String applicationName;
    public IdWithLocalizedValueInfo<PayloadSaveStrategy> payloadStrategy;
}
