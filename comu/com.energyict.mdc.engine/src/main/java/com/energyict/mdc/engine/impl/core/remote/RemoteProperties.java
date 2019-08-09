package com.energyict.mdc.engine.impl.core.remote;

import java.util.Properties;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 20/05/2015 - 13:43
 */
public class RemoteProperties {

    private static final int KB = 1024;

    public static final String REMOTE_QUERY_API_URL_PROPERTY = "remoteQueryApiUrl";
    public static final String MAX_MESSAGE_SIZE = "maxMessageSize";
    private static final int MAX_MESSAGE_SIZE_DEFAULT = 128 * KB;
    public static final String TIMEOUT_PROPERTY = "timeout";
    private static final int TIMEOUT_DEFAULT = 300000;  //5 minutes
    public static final String RETRIES_PROPERTY = "retries";
    private static final int DEFAULT_RETRIES = 3;
    public static final String RECONNECTION_ATTEMPTS = "reconnectionAttempts";
    private static final int RECONNECTION_ATTEMPTS_DEFAULT = 10;
    public static final String RECONNECTION_DELAY = "reconnectionDelay";
    private static final int RECONNECTION_DELAY_DEFAULT = 60000;    //1 minute
    public static final String COMPRESS_QUERY_DATA = "compressQueryData";
    private static final Boolean COMPRESS_QUERY_DATA_DEFAULT = true;

    private final Properties properties;

    public RemoteProperties() {
        this.properties = new Properties();
    }

    public RemoteProperties(Properties properties) {
        this.properties = properties;
    }

    public String getRemoteQueryApiUrl() {
        return properties.getProperty(REMOTE_QUERY_API_URL_PROPERTY);
    }

    public Boolean getCompressQueryData() {
        try {
            return Boolean.parseBoolean(properties.getProperty(COMPRESS_QUERY_DATA, String.valueOf(COMPRESS_QUERY_DATA_DEFAULT)));
        } catch (Exception e) {
            return COMPRESS_QUERY_DATA_DEFAULT;
        }
    }

    public int getMaxMessageSize() {
        try {
            return Integer.parseInt(properties.getProperty(MAX_MESSAGE_SIZE, String.valueOf(MAX_MESSAGE_SIZE_DEFAULT)));
        } catch (NumberFormatException e) {
            return MAX_MESSAGE_SIZE_DEFAULT;
        }
    }

    public long getTimeout() {
        try {
            return Long.parseLong(properties.getProperty(TIMEOUT_PROPERTY, String.valueOf(TIMEOUT_DEFAULT)));
        } catch (NumberFormatException e) {
            return TIMEOUT_DEFAULT;
        }
    }

    public int getRetries() {
        try {
            return Integer.parseInt(properties.getProperty(RETRIES_PROPERTY, String.valueOf(DEFAULT_RETRIES)));
        } catch (NumberFormatException e) {
            return DEFAULT_RETRIES;
        }
    }

    public long getReconnectionDelay() {
        try {
            return Integer.parseInt(properties.getProperty(RECONNECTION_DELAY, String.valueOf(RECONNECTION_DELAY_DEFAULT)));
        } catch (NumberFormatException e) {
            return RECONNECTION_DELAY_DEFAULT;
        }
    }

    public long getReconnectionAttempts() {
        try {
            return Integer.parseInt(properties.getProperty(RECONNECTION_ATTEMPTS, String.valueOf(RECONNECTION_ATTEMPTS_DEFAULT)));
        } catch (NumberFormatException e) {
            return RECONNECTION_ATTEMPTS_DEFAULT;
        }
    }
}