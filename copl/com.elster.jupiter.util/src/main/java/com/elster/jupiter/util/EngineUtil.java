package com.elster.jupiter.util;

import com.google.common.base.Strings;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class EngineUtil {

    /**
     * The name of the property that provides the type
     * of the server on which the AppServer is running.
     * When not set, the default online comserver is used.
     */
    public static final String SERVER_TYPE_PROPERTY_NAME = "com.elster.jupiter.server.type";

    enum EngineMode {
        ONLINE,
        OFFLINE;
    }

    public static boolean isOnlineMode() {
        Bundle bundle = FrameworkUtil.getBundle(EngineUtil.class);
        if (bundle != null) {
            String serverType = bundle.getBundleContext().getProperty(SERVER_TYPE_PROPERTY_NAME);
            return Strings.isNullOrEmpty(serverType) || EngineMode.ONLINE.name().equalsIgnoreCase(serverType);
        }
        return true;
    }

    public static boolean isOfflineMode() {
        Bundle bundle = FrameworkUtil.getBundle(EngineUtil.class);
        if (bundle != null) {
            String serverType = bundle.getBundleContext().getProperty(SERVER_TYPE_PROPERTY_NAME);
            return EngineMode.OFFLINE.name().equalsIgnoreCase(serverType);
        }
        return false;
    }

}
