package com.elster.jupiter.systemadmin.rest.response;

import com.elster.jupiter.systemadmin.Properties;

import java.util.Date;
import java.util.Map;

public class LicenseShortInfo {
    protected String applicationtag;
    protected String application;
    protected String type;
    protected String status;
    protected String description;
    protected long expires;

    public LicenseShortInfo() {}
    public LicenseShortInfo(Properties props) {
        //Map<String, String> props = properties.getProperties();
        this.applicationtag = props.getProperty(LicenseProperties.TAG.getName());
        this.application = props.getProperty(LicenseProperties.NAME.getName());
        this.type = props.getProperty(LicenseProperties.TYPE.getName());
        this.status = props.getProperty(LicenseProperties.STATUS.getName());
        this.description = props.getProperty(LicenseProperties.DESCRIPTION.getName());
        Date date = new Date(props.getProperty(LicenseProperties.EXPIRES.getName()));
        this.expires = date.getTime();
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getType() {
        return type;
    }

    public String getApplication() {
        return application;
    }

    public String getApplicationtag() {
        return applicationtag;
    }

    public long getExpires() {
        return expires;
    }
}
