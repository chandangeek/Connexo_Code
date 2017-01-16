package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.rest.util.IdWithLocalizedValueInfo;
import com.elster.jupiter.rest.util.LongIdWithNameInfo;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;


public class EndPointConfigurationInfo {
    public long id;
    public long version;
    public String name;
    public String webServiceName;
    public String url;
    public IdWithLocalizedValueInfo<String> logLevel;
    public Boolean tracing;
    public String traceFile;
    public Boolean httpCompression;
    public Boolean schemaValidation;
    public Boolean active;
    public Boolean available;
    public IdWithLocalizedValueInfo<EndPointAuthentication> authenticationMethod;
    public String type;
    public String previewUrl;
    public LongIdWithNameInfo group;
}
