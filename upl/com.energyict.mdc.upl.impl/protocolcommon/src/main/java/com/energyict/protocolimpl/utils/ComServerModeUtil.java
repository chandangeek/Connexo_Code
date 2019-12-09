package com.energyict.protocolimpl.utils;

import com.google.common.base.Strings;
import org.osgi.framework.FrameworkUtil;

import static com.energyict.protocolimpl.utils.ComServerModeUtil.ComServerMode.OFFLINE;
import static com.energyict.protocolimpl.utils.ComServerModeUtil.ComServerMode.ONLINE;

public class ComServerModeUtil {

    private static String PROPERTY_NAME = "com.elster.jupiter.server.type";
    private static String  OFF_LINE= "OFFLINE";

    enum ComServerMode {
        ONLINE,
        OFFLINE;
    }

    public static ComServerMode getMode() {
        String platform = FrameworkUtil.getBundle(ComServerModeUtil.class).getBundleContext().getProperty(PROPERTY_NAME);
        if (!Strings.isNullOrEmpty(platform) && platform.equalsIgnoreCase(OFF_LINE)) {
            return OFFLINE;
        }
        return ONLINE;
    }

    public static boolean isOffLine() {
        return getMode() == OFFLINE;
    }

    public static boolean isOnline() {
        return getMode() == ONLINE;
    }


}
