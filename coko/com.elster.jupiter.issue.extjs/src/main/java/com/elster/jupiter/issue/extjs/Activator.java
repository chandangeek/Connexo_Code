package com.elster.jupiter.issue.extjs;

import com.elster.jupiter.http.whiteboard.impl.HttpActivator;

public class Activator extends HttpActivator {

    public static final String HTTP_RESOURCE_ALIAS = "/isu";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/issue";

    public Activator() {
        super(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME);
        // EXAMPLE Below is how to enable development mode.
//        super(HTTP_RESOURCE_ALIAS, "/home/lvz/Documents/Workspace/Jupiter/com.elster.jupiter.issue.extjs/src/main/web/js/issue", true);
    }

}