package com.elster.jupiter.soap.whiteboard.cxf;

import java.util.List;

/**
 * This class represents a configuration for an endpoint. There will be a 1-on-1 relation between a javax.xml.ws.EndPoint
 * and EndPointConfiguration, but this class was created to persist config of the actual endpoint. Is was named
 * EndPointConfiguration and not just EndPoint to avoid confusion with javax.xml.ws.endPoint. The relation is established/managed
 * by {ManagedEndpoint}.
 */
public interface EndPointConfiguration {
    long getId();

    String getName();

    String getUrl();

    String getWebServiceName();

    LogLevel getLogLevel();

    boolean isTracing();

    boolean isHttpCompression();

    boolean isSchemaValidation();

    boolean isActive();

    void setName(String name);

    void setUrl(String url);

    void setWebServiceName(String webServiceName);

    void setLogLevel(LogLevel logLevel);

    void setTracing(boolean tracing);

    void setHttpCompression(boolean httpCompression);

    void setSchemaValidation(boolean schemaValidation);

    void setActive(boolean active);

    void save();

    /**
     * Log an entry for this end point (config). As real endpoints are runtime objects without persistent end, logging is done on the config instead.
     *
     * @param logLevel The level to log on.
     * @param message The log message
     */
    void log(LogLevel logLevel, String message);

    /**
     * Retrieve a log of all end point entries. List is sorted, most recent message comes first.
     *
     * @return List of logs
     */
    List<EndPointLog> getLogs();
}
