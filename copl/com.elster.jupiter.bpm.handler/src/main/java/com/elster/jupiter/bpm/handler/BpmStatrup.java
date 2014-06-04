package com.elster.jupiter.bpm.handler;

import org.osgi.framework.BundleContext;

public class BpmStatrup {

    private static final String BPM_URL = "com.elster.jupiter.bpm.url";
    private static final String BPM_USER = "com.elster.jupiter.bpm.user";
    private static final String BPM_PASSWORD = "com.elster.jupiter.bpm.password";

    private static final String DEFAULT_BPM_URL = "http://localhost:8081/jbpm-console";
    private static final String DEFAULT_BPM_USER = "admin";
    private static final String DEFAULT_BPM_PASSWORD = "admin";

    private String url;
    private String user;
    private String password;

    private static BpmStatrup instance;

    private BpmStatrup(BundleContext context){
        initBpmStartup(context);
    }

    public static BpmStatrup getInstance() {
        return instance;
    }

    public static void init(BundleContext context){
        instance = new BpmStatrup(context);
    }

    private void initBpmStartup(BundleContext context) {
        url = context.getProperty(BPM_URL);
        user = context.getProperty(BPM_USER);
        password = context.getProperty(BPM_PASSWORD);
        if (url == null) {
            url = this.DEFAULT_BPM_URL;
        }
        if (user == null) {
            user = this.DEFAULT_BPM_USER;
        }
        if (password == null) {
            password = this.DEFAULT_BPM_PASSWORD;
        }
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}

