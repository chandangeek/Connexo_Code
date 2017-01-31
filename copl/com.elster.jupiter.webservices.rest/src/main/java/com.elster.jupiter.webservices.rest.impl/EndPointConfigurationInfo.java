/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.rest.util.IdWithLocalizedValueInfo;
import com.elster.jupiter.rest.util.LongIdWithNameInfo;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;

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
    public String password;
    public IdWithLocalizedValueInfo<WebServiceDirection> direction;
    public String type;
}
