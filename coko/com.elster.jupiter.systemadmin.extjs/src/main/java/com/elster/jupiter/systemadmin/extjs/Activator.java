package com.elster.jupiter.systemadmin.extjs;

import com.elster.jupiter.http.whiteboard.HttpActivator;

public class Activator extends HttpActivator {

    public static final String HTTP_RESOURCE_ALIAS = "/sam";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/systemadmin";

    public Activator() {
        super(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME);
        // EXAMPLE Below is how to enable development mode.
//        super(HTTP_RESOURCE_ALIAS, "/home/lvz/Documents/Workspace/Jupiter/com.elster.jupiter.systemadmin.extjs/src/main/web/js/systemadmin", true);
    }

}