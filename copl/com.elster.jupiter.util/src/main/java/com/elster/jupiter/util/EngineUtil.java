package com.elster.jupiter.util;

import org.osgi.framework.BundleContext;

public class EngineUtil {

    /**
     * The name of the property that provides the type
     * of the server on which the AppServer is running.
     * When not set, the default online comserver is used.
     */
    public static final String SERVER_TYPE_PROPERTY_NAME = "com.elster.jupiter.server.type";

    public static boolean isOnlineMode(BundleContext context) {
        String serverType = context.getProperty(SERVER_TYPE_PROPERTY_NAME);
        return serverType == null || serverType.equalsIgnoreCase("online");
    }
}
