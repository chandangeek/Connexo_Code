/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest.imp.response;

import com.elster.jupiter.license.License;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.systemadmin.rest.imp.resource.LicenseStatusTranslationKeys;
import com.elster.jupiter.systemadmin.rest.imp.resource.LicenseTypeTranslationKeys;

import javax.inject.Inject;
import java.util.AbstractMap;
import java.util.stream.Collectors;

public class LicenseInfoFactory {

    private final NlsService nlsService;
    private final Thesaurus thesaurus;

    @Inject
    public LicenseInfoFactory(NlsService nlsService, Thesaurus thesaurus) {
        this.nlsService = nlsService;
        this.thesaurus = thesaurus;
    }

    public LicenseShortInfo asShortInfo(License license) {
        LicenseShortInfo info = new LicenseShortInfo();
        setCommonInfo(license, info);
        return info;
    }

    public LicenseInfo asInfo(License license) {
        LicenseInfo info = new LicenseInfo();
        setCommonInfo(license, info);
        info.type = thesaurus.getFormat(LicenseTypeTranslationKeys.getTranslatedName(license.getType())).format();
        info.description = license.getDescription();
        info.validfrom = license.getActivation();
        info.graceperiod = license.getGracePeriodInDays();
        info.content = license.getLicensedValues().entrySet().stream()
                .map(property -> new AbstractMap.SimpleEntry<>(
                        thesaurus.getString(property.getKey().toString(), property.getKey().toString()),
                        property.getValue()
                ))
                .collect(Collectors.toSet());
        return info;
    }

    private void setCommonInfo(License license, LicenseShortInfo info) {
        info.applicationkey = license.getApplicationKey();
        info.applicationname = nlsService.getThesaurus(license.getApplicationKey(), Layer.DOMAIN)
                .getString(license.getApplicationKey(), license.getApplicationKey());
        info.status = thesaurus.getFormat(LicenseStatusTranslationKeys.getTranslatedName(license.getStatus())).format();
        info.expires = license.getExpiration();
    }
}
