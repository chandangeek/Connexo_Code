package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.domain.util.Finder;

import java.util.List;

/**
 * This class represents a configuration for an endpoint. There will be a 1-on-1 relation between a javax.xml.ws.EndPoint
 * and EndPointConfiguration, but this class was created to persist config of the actual endpoint. Is was named
 * EndPointConfiguration and not just EndPoint to avoid confusion with javax.xml.ws.endPoint. The relation is established/managed
 * by {ManagedEndpoint}. The AppServers will create the actual endpoint based on configuration as found in an EndPointConfiguration.
 */
public interface EndPointConfiguration {
    long getId();

    String getName();

    String getUrl();

    String getWebServiceName();

    LogLevel getLogLevel();

    /**
     * If tracing is on, detailed information for this end point will be logged to the trace file
     */
    boolean isTracing();

    /**
     * The name of the file where trace log information will be stored
     **/
    String getTraceFile();

    boolean isHttpCompression();

    boolean isSchemaValidation();

    boolean isActive();

    long getVersion();

    void setName(String name);

    void setUrl(String url);

    /**
     * This name identifies the web service for which this configuration applies
     */
    void setWebServiceName(String webServiceName);

    void setLogLevel(LogLevel logLevel);

    EndPointAuthentication getAuthenticationMethod();

    void setAuthenticationMethod(EndPointAuthentication authenticated);

    /**
     * If tracing is on, detailed information for this end point will be logged to the trace file
     */
    void setTracing(boolean tracing);

    /**
     * Sets the name of the tracing file
     *
     * @param traceFile The name of the file where trace log information will be stored
     */
    void setTraceFile(String traceFile);

    void setHttpCompression(boolean httpCompression);

    void setSchemaValidation(boolean schemaValidation);

    /**
     * This method make a config change, but does not trigger appservers to publish/revoke the actual endpoint. Use
     * EndPointConfigurationService::activate()/~::deactivate() for this purpose.
     *
     * @param active
     */
//    void setActive(boolean active);

    void save();

    /**
     * Log an entry for this end point (config). As real endpoints are runtime objects without persistent end, logging is done on the config instead.
     *
     * @param logLevel The level to log on.
     * @param message The log message
     */
    void log(LogLevel logLevel, String message);

    /**
     * Retrieve a finder of logs of all end point entries. List is sorted, most recent message comes first.
     *
     * @return Finder of logs
     */
    Finder<EndPointLog> getLogs();

}
