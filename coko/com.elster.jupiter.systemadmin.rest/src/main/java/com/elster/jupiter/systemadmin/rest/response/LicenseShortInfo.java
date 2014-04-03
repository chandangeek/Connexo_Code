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
    public LicenseShortInfo(Properties properties) {
        Map<String, String> props = properties.getProperties();
        this.applicationtag = props.get(LicenseProperties.TAG.getName());
        this.application = props.get(LicenseProperties.NAME.getName());
        this.type = props.get(LicenseProperties.TYPE.getName());
        this.status = props.get(LicenseProperties.STATUS.getName());
        this.description = props.get(LicenseProperties.DESCRIPTION.getName());
        Date date = new Date(props.get(LicenseProperties.EXPIRES.getName()));
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
