package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.QualityCodeSystem;

import aQute.bnd.annotation.ProviderType;

import java.util.Set;

@ProviderType
public interface ReadingQualityTypeFilter extends ReadingQualityIndexFilter {

    /**
     * Defines {@link QualityCodeSystem} to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * Pls use exclusively with {@link #ofQualitySystems(Set)} because call of each one
     * overrides previously defined criterion.
     * None of these 2 methods called means any {@link QualityCodeSystem} matching.
     *
     * @param system {@link QualityCodeSystem} to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * @return the self to proceed with search criteria definition or collect the results.
     */
    ReadingQualityWithTypeFilter ofQualitySystem(QualityCodeSystem system);

    /**
     * Defines a set of {@link QualityCodeSystem QualityCodeSystems} to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * Pls use exclusively with {@link #ofQualitySystem(QualityCodeSystem)} because call of each one
     * overrides previously defined criterion.
     * None of these 2 methods called means any {@link QualityCodeSystem} matching.
     *
     * @param systems a set of {@link QualityCodeSystem QualityCodeSystems} to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * @return the self to proceed with search criteria definition or collect the results.
     */
    ReadingQualityWithTypeFilter ofQualitySystems(Set<QualityCodeSystem> systems);

}
