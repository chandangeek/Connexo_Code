package com.elster.jupiter.systemadmin.rest.imp.response;

import com.elster.jupiter.license.License;
import com.elster.jupiter.nls.NlsService;

import java.util.Map;
import java.util.Set;

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
        this.validfrom = license.getActivation().toEpochMilli();
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
