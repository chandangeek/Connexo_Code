package com.elster.jupiter.systemadmin.rest.imp.response;

import com.elster.jupiter.license.License;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.systemadmin.rest.imp.SystemApplication;

import java.util.*;
import java.util.stream.Collectors;

public class LicenseInfo extends LicenseShortInfo {

    private long validfrom;
    private int graceperiod;
    private String type;
    private String description;
    private Set<Map.Entry<Object, Object>> content = new HashSet<>();

    public LicenseInfo() {
    }

    public LicenseInfo(NlsService nlsService, License license) {
        super(nlsService, license);
        this.type = license.getType().name().toLowerCase();
        this.description = license.getDescription();
        this.validfrom = license.getActivation().toEpochMilli();
        this.graceperiod = license.getGracePeriodInDays();
        Thesaurus thesaurus = nlsService.getThesaurus(SystemApplication.COMPONENT_NAME, Layer.REST);
        content = license.getLicensedValues().entrySet()
                .stream()
                .map(property -> new AbstractMap.SimpleEntry<Object, Object>(
                        thesaurus.getString(property.getKey().toString(), property.getKey().toString()),
                        property.getValue()
                ))
                .collect(Collectors.toCollection(HashSet::new));
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
