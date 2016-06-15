package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;

/**
 * Model the configuration to create an actual endpoint. The latter is a purely runtime entity.
 */
public class EndPointConfigurationInfo {
    public long id;
    public Long version;
    public String name;
    public String webServiceName;
    public String url;
    public IdWithDisplayValueInfo<String> logLevel;
    public Boolean tracing;
    public String traceFile;
    public Boolean httpCompression;
    public Boolean schemaValidation;
    public Boolean active;
    public Boolean authenticated;
    public String username;
    public String password;
    public EndPointConfigDirection direction;
    public String type = "SOAP";
}
