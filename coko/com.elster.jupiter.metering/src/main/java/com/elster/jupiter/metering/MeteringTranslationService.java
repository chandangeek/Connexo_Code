/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;

import aQute.bnd.annotation.ProviderType;

/**
 * Provides translation services for metering related concepts.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-29 (14:31)
 */
@ProviderType
public interface MeteringTranslationService {

    String getDisplayName(QualityCodeIndex index);

    String getDisplayName(QualityCodeSystem system);

    String getDisplayName(QualityCodeCategory category);

    String getDisplayName(ServiceKind serviceKind);

}