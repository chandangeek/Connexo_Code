package com.elster.jupiter.systemadmin.rest.response;

import com.elster.jupiter.license.License;

import java.util.Date;
import java.util.Map;
import java.util.Properties;

public class LicenseShortInfo {
    protected String applicationtag;
    protected String status;
    protected Long expires;

    public LicenseShortInfo() {}
    public LicenseShortInfo(License lic) {
        this.applicationtag = lic.getApplicationKey();
        this.status = lic.getStatus().name().toLowerCase();
        this.expires = lic.getExpiration().getTime();
    }

    public String getStatus() {
        return status;
    }

    public String getApplicationtag() {
        return applicationtag;
    }

    public Long getExpires() {
        return expires;
    }
}
