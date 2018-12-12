/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Provides an implementation for the {@link MeteringTranslationService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-29 (14:35)
 */
class MeteringTranslationServiceImpl implements MeteringTranslationService {

    private final Thesaurus thesaurus;

    MeteringTranslationServiceImpl(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public String getDisplayName(QualityCodeIndex index) {
        return this.thesaurus.getFormat(index.getTranslationKey()).format();
    }

    @Override
    public String getDisplayName(QualityCodeSystem system) {
        return this.thesaurus.getFormat(system.getTranslationKey()).format();
    }

    @Override
    public String getDisplayName(QualityCodeCategory category) {
        return this.thesaurus.getFormat(category.getTranslationKey()).format();
    }

    @Override
    public String getDisplayName(ServiceKind serviceKind) {
        return this.thesaurus.getFormat(serviceKind).format();
    }

}