package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;

/**
 * Model the configuration to create an actual endpoint. The latter is a purely runtime entity.
 */
public class EndPointConfigurationInfo {
    public long id;
    public long version;
    public String name;
    public String webServiceName;
    public String url;
    public IdWithDisplayValueInfo<String> logLevel;
    public Boolean tracing;
    public Boolean httpCompression;
    public Boolean schemaValidation;
    public Boolean active;
    public Boolean authenticated;
    public String username;
    public String password;
    public EndPointConfigType type;
}
