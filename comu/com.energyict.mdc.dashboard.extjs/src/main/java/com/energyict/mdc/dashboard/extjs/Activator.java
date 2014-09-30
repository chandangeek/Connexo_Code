package com.energyict.mdc.dashboard.extjs;

import com.elster.jupiter.http.whiteboard.HttpActivator;

public class Activator extends HttpActivator {

    public static final String HTTP_RESOURCE_ALIAS = "/dsh";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/dsh";

    public Activator() {
        super(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME);
        // EXAMPLE: Below is how to enable local development mode.
//        super(HTTP_RESOURCE_ALIAS, "/home/lvz/Documents/Workspace/Jupiter/com.energyict.mdc.dashboard.extjs/src/main/web/js/dsh", true);
    }

}