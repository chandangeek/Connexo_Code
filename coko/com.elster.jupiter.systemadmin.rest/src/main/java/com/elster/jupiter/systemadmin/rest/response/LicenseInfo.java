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
        this.applicationtag = props.get(LicenseProperties.TAG.getName());
        props.remove(LicenseProperties.TAG.getName());
        this.application = props.get(LicenseProperties.NAME.getName());
        props.remove(LicenseProperties.NAME.getName());
        this.type = props.get(LicenseProperties.TYPE.getName());
        props.remove(LicenseProperties.TYPE.getName());
        this.status = props.get(LicenseProperties.STATUS.getName());
        props.remove(LicenseProperties.STATUS.getName());
        this.description = props.get(LicenseProperties.DESCRIPTION.getName());
        props.remove(LicenseProperties.DESCRIPTION.getName());
        this.validfrom = Long.parseLong(props.get(LicenseProperties.VALID_FROM.getName()));
        props.remove(LicenseProperties.VALID_FROM.getName());
        this.graceperiod = props.get(LicenseProperties.GRACEPERIOD.getName());
        props.remove(LicenseProperties.GRACEPERIOD.getName());
        Date date = new Date(props.get(LicenseProperties.EXPIRES.getName()));
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
