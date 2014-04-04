package com.elster.jupiter.systemadmin.rest.response;

import com.elster.jupiter.systemadmin.Properties;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class LicenseInfo extends LicenseShortInfo {

    private long validfrom;
    private String graceperiod;
    private List<Map.Entry<String, String>> content;

    public LicenseInfo() {
        this.content = new ArrayList<>();
    }

    public LicenseInfo(Properties properties) {
        this();
        Map <String, String> props = properties.getProperties();
        this.applicationtag = properties.getProperty(LicenseProperties.TAG.getName());
        props.remove(LicenseProperties.TAG.getName());
        this.application = properties.getProperty(LicenseProperties.NAME.getName());
        props.remove(LicenseProperties.NAME.getName());
        this.type = properties.getProperty(LicenseProperties.TYPE.getName());
        props.remove(LicenseProperties.TYPE.getName());
        this.status = properties.getProperty(LicenseProperties.STATUS.getName());
        props.remove(LicenseProperties.STATUS.getName());
        this.description = properties.getProperty(LicenseProperties.DESCRIPTION.getName());
        props.remove(LicenseProperties.DESCRIPTION.getName());
        this.validfrom = Long.parseLong(properties.getProperty(LicenseProperties.VALID_FROM.getName()));
        props.remove(LicenseProperties.VALID_FROM.getName());
        this.graceperiod = properties.getProperty(LicenseProperties.GRACEPERIOD.getName());
        props.remove(LicenseProperties.GRACEPERIOD.getName());
        Date date = new Date(properties.getProperty(LicenseProperties.EXPIRES.getName()));
        this.expires = date.getTime();
        props.remove(LicenseProperties.EXPIRES.getName());
        this.content.addAll(props.entrySet());
    }

    public List<Map.Entry<String, String>> getContent() {
        return this.content;
    }
    public long getExpires() {
        return expires;
    }

    public String getGraceperiod() {
        return graceperiod;
    }

    public long getValidfrom() {
        return validfrom;
    }
}
