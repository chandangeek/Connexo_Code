/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest.imp.response;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.system.BundleType;
import com.elster.jupiter.systemadmin.rest.imp.resource.BundleTypeTranslationKeys;

import javax.inject.Inject;


public class BundleTypeInfoFactory {
    private final Thesaurus thesaurus;

    @Inject
    public BundleTypeInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public BundleTypeInfo asInfo(BundleType bundleType) {
        BundleTypeInfo bundleTypeInfo = new BundleTypeInfo();
        bundleTypeInfo.id = bundleType.getId();
        bundleTypeInfo.name = thesaurus.getFormat(BundleTypeTranslationKeys.getTranslatedName(bundleType)).format();
        return bundleTypeInfo;
    }
}
