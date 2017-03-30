/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface ReadingQualityWithTypeFetcher extends ReadingQualityFetcher {

    /**
     * Starts declaration of an alternative {@link ReadingQualityType} criteria group to look for.
     * Logically 'or' applies here to {@link ReadingQualityType} related criteria only.
     *
     * @return The self to proceed with search criteria definition.
     */
    ReadingQualityTypeFilter orOfAnotherType();

    /**
     * Starts declaration of an alternative {@link ReadingQualityType} criteria group to look for,
     * keeping previously defined {@link QualityCodeSystem QualityCodeSystems} related criterion.
     * Logically 'or' applies here to {@link QualityCodeCategory}
     * and {@link QualityCodeIndex} related criteria only.
     *
     * @return The self to proceed with search criteria definition.
     */
    ReadingQualityIndexFilter orOfAnotherTypeInSameSystems();
}
