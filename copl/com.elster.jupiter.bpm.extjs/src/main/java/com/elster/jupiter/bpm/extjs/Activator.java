package com.elster.jupiter.bpm.extjs;

import com.elster.jupiter.http.whiteboard.impl.HttpActivator;

public class Activator extends HttpActivator {

    public static final String HTTP_RESOURCE_ALIAS = "/bpm";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/bpm";

    public Activator() {
        super(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME);
        // EXAMPLE: Below is how to enable local development mode.
//        super(HTTP_RESOURCE_ALIAS, "/home/lvz/Documents/Workspace/Jupiter/com.elster.jupiter.bpm.extjs/src/main/web/js/bpm", true);
    }

}
