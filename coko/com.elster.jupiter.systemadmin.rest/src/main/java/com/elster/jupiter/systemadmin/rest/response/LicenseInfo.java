package com.elster.jupiter.systemadmin.rest.response;

import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.nls.NlsService;

import javax.management.monitor.StringMonitor;
import java.util.*;

public class LicenseInfo extends LicenseShortInfo {

    private long validfrom;
    private int graceperiod;
    private String type;
    private String description;
    private Set<Map.Entry<Object, Object>> content;

    public LicenseInfo() {
    }

    public LicenseInfo(NlsService nlsService, License license) {
        super(nlsService, license);
        this.type = license.getType().name().toLowerCase();
        this.description = license.getDescription();
        this.validfrom = license.getActivation().getTime();
        this.graceperiod = license.getGracePeriodInDays();
        this.content = license.getLicensedValues().entrySet();
    }

    public Set<Map.Entry<Object, Object>> getContent() {
        return this.content;
    }
    public Long getExpires() {
        return expires;
    }

    public int getGraceperiod() {
        return graceperiod;
    }

    public long getValidfrom() {
        return validfrom;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}
