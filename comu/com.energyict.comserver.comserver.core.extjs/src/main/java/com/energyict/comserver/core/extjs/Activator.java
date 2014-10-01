package com.energyict.comserver.core.extjs;

import com.elster.jupiter.http.whiteboard.HttpActivator;

public class Activator extends HttpActivator {

    public static final String HTTP_RESOURCE_ALIAS = "/mdc";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/mdc";

    public Activator() {
        super(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME);
        // EXAMPLE: Below is how to enable development mode.
//        super(HTTP_RESOURCE_ALIAS, "/home/lvz/Documents/Workspace/Jupiter/com.energyict.comserver.comserver.core.extjs/src/main/web/js/mdc", true);
    }

}