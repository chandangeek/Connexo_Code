package com.elster.jupiter.users.extjs;

import com.elster.jupiter.http.whiteboard.impl.HttpActivator;

public class Activator extends HttpActivator {

    public static final String HTTP_RESOURCE_ALIAS = "/usr";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/usr";

    public Activator() {
        super(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME);
        // EXAMPLE Below is how to enable development mode.
//        super(HTTP_RESOURCE_ALIAS, "/home/lvz/Documents/Workspace/Jupiter/com.elster.jupiter.users.extjs/src/main/web/js/usr", true);
    }

}