/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;

import aQute.bnd.annotation.ProviderType;

import java.util.Set;

@ProviderType
public interface ReadingQualityIndexFilter {

    /**
     * Defines {@link QualityCodeIndex} to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * Pls use exclusively with {@link #ofQualityIndex(QualityCodeCategory, int)},
     * {@link #ofQualityIndices(Set)}, {@link #ofQualityIndices(QualityCodeCategory, Set)},
     * {@link #ofAnyQualityIndexInCategory(QualityCodeCategory)}, {@link #ofAnyQualityIndexInCategories(Set)}
     * because call of each one overrides previously defined criterion.
     * None of these methods called means any index in any {@link QualityCodeCategory} matching.
     * Once any of these methods is called it's allowed to define an alternative set of
     * {@link ReadingQualityType} related search criteria by calling {@link ReadingQualityWithTypeFetcher#orOfAnotherType()}
     * or {@link ReadingQualityWithTypeFetcher#orOfAnotherTypeInSameSystems()}.
     *
     * @param index {@link QualityCodeIndex} to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * @return The self to proceed with search criteria definition or collect the results.
     */
    ReadingQualityWithTypeFetcher ofQualityIndex(QualityCodeIndex index);

    /**
     * Defines {@link QualityCodeCategory} and index to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * Pls use exclusively with {@link #ofQualityIndex(QualityCodeIndex)},
     * {@link #ofQualityIndices(Set)}, {@link #ofQualityIndices(QualityCodeCategory, Set)},
     * {@link #ofAnyQualityIndexInCategory(QualityCodeCategory)}, {@link #ofAnyQualityIndexInCategories(Set)}
     * because call of each one overrides previously defined criterion.
     * None of these methods called means any index in any {@link QualityCodeCategory} matching.
     * Once any of these methods is called it's allowed to define an alternative set of
     * {@link ReadingQualityType} related search criteria by calling {@link ReadingQualityWithTypeFetcher#orOfAnotherType()}
     * or {@link ReadingQualityWithTypeFetcher#orOfAnotherTypeInSameSystems()}.
     *
     * @param category {@link QualityCodeCategory} to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * @param index Index in {@code category} to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * @return The self to proceed with search criteria definition or collect the results.
     */
    ReadingQualityWithTypeFetcher ofQualityIndex(QualityCodeCategory category, int index);

    /**
     * Defines {@link QualityCodeIndex QualityCodeIndices} to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * Pls use exclusively with {@link #ofQualityIndex(QualityCodeIndex)},
     * {@link #ofQualityIndex(QualityCodeCategory, int)}, {@link #ofQualityIndices(QualityCodeCategory, Set)},
     * {@link #ofAnyQualityIndexInCategory(QualityCodeCategory)}, {@link #ofAnyQualityIndexInCategories(Set)}
     * because call of each one overrides previously defined criterion.
     * None of these methods called means any index in any {@link QualityCodeCategory} matching.
     * Once any of these methods is called it's allowed to define an alternative set of
     * {@link ReadingQualityType} related search criteria by calling {@link ReadingQualityWithTypeFetcher#orOfAnotherType()}
     * or {@link ReadingQualityWithTypeFetcher#orOfAnotherTypeInSameSystems()}.
     *
     * @param indices {@link QualityCodeIndex QualityCodeIndices} to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * @return The self to proceed with search criteria definition or collect the results.
     */
    ReadingQualityWithTypeFetcher ofQualityIndices(Set<QualityCodeIndex> indices);

    /**
     * Defines {@link QualityCodeCategory} and indices to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * Pls use exclusively with {@link #ofQualityIndex(QualityCodeIndex)},
     * {@link #ofQualityIndex(QualityCodeCategory, int)}, {@link #ofQualityIndices(Set)},
     * {@link #ofAnyQualityIndexInCategory(QualityCodeCategory)}, {@link #ofAnyQualityIndexInCategories(Set)}
     * because call of each one overrides previously defined criterion.
     * None of these methods called means any index in any {@link QualityCodeCategory} matching.
     * Once any of these methods is called it's allowed to define an alternative set of
     * {@link ReadingQualityType} related search criteria by calling {@link ReadingQualityWithTypeFetcher#orOfAnotherType()}
     * or {@link ReadingQualityWithTypeFetcher#orOfAnotherTypeInSameSystems()}.
     *
     * @param category {@link QualityCodeCategory} to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * @param indices Indices in {@code category} to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * @return The self to proceed with search criteria definition or collect the results.
     */
    ReadingQualityWithTypeFetcher ofQualityIndices(QualityCodeCategory category, Set<Integer> indices);

    /**
     * Defines {@link QualityCodeCategory} to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * Pls use exclusively with {@link #ofQualityIndex(QualityCodeIndex)},
     * {@link #ofQualityIndex(QualityCodeCategory, int)}, {@link #ofQualityIndices(Set)},
     * {@link #ofQualityIndices(QualityCodeCategory, Set)}, {@link #ofAnyQualityIndexInCategories(Set)}
     * because call of each one overrides previously defined criterion.
     * None of these methods called means any index in any {@link QualityCodeCategory} matching.
     * Once any of these methods is called it's allowed to define an alternative set of
     * {@link ReadingQualityType} related search criteria by calling {@link ReadingQualityWithTypeFetcher#orOfAnotherType()}
     * or {@link ReadingQualityWithTypeFetcher#orOfAnotherTypeInSameSystems()}.
     *
     * @param category {@link QualityCodeCategory} to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * @return The self to proceed with search criteria definition or collect the results.
     */
    ReadingQualityWithTypeFetcher ofAnyQualityIndexInCategory(QualityCodeCategory category);

    /**
     * Defines {@link QualityCodeCategory QualityCodeCategories} to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * Pls use exclusively with {@link #ofQualityIndex(QualityCodeIndex)},
     * {@link #ofQualityIndex(QualityCodeCategory, int)}, {@link #ofQualityIndices(Set)},
     * {@link #ofQualityIndices(QualityCodeCategory, Set)}, {@link #ofAnyQualityIndexInCategory(QualityCodeCategory)}
     * because call of each one overrides previously defined criterion.
     * None of these methods called means any index in any {@link QualityCodeCategory} matching.
     * Once any of these methods is called it's allowed to define an alternative set of
     * {@link ReadingQualityType} related search criteria by calling {@link ReadingQualityWithTypeFetcher#orOfAnotherType()}
     * or {@link ReadingQualityWithTypeFetcher#orOfAnotherTypeInSameSystems()}.
     *
     * @param categories {@link QualityCodeCategory QualityCodeCategories} to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * @return The self to proceed with search criteria definition or collect the results.
     */
    ReadingQualityWithTypeFetcher ofAnyQualityIndexInCategories(Set<QualityCodeCategory> categories);
}
