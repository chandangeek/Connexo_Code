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
     * {@link ReadingQualityType} related search criteria by calling {@link ReadingQualityWithTypeFilter#orOfAnotherType()}
     * or {@link ReadingQualityWithTypeFilter#orOfAnotherTypeInSameSystems()}
     *
     * @param index {@link QualityCodeIndex} to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * @return the self to proceed with search criteria definition or collect the results.
     */
    ReadingQualityWithTypeFilter ofQualityIndex(QualityCodeIndex index);

    /**
     * Defines {@link QualityCodeCategory} and index to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * Pls use exclusively with {@link #ofQualityIndex(QualityCodeIndex)},
     * {@link #ofQualityIndices(Set)}, {@link #ofQualityIndices(QualityCodeCategory, Set)},
     * {@link #ofAnyQualityIndexInCategory(QualityCodeCategory)}, {@link #ofAnyQualityIndexInCategories(Set)}
     * because call of each one overrides previously defined criterion.
     * None of these methods called means any index in any {@link QualityCodeCategory} matching.
     * Once any of these methods is called it's allowed to define an alternative set of
     * {@link ReadingQualityType} related search criteria by calling {@link ReadingQualityWithTypeFilter#orOfAnotherType()}
     * or {@link ReadingQualityWithTypeFilter#orOfAnotherTypeInSameSystems()}
     *
     * @param category {@link QualityCodeCategory} to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * @param index index in <code>category</code> to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * @return the self to proceed with search criteria definition or collect the results.
     */
    ReadingQualityWithTypeFilter ofQualityIndex(QualityCodeCategory category, int index);

    /**
     * Defines {@link QualityCodeIndex QualityCodeIndices} to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * Pls use exclusively with {@link #ofQualityIndex(QualityCodeIndex)},
     * {@link #ofQualityIndex(QualityCodeCategory, int)}, {@link #ofQualityIndices(QualityCodeCategory, Set)},
     * {@link #ofAnyQualityIndexInCategory(QualityCodeCategory)}, {@link #ofAnyQualityIndexInCategories(Set)}
     * because call of each one overrides previously defined criterion.
     * None of these methods called means any index in any {@link QualityCodeCategory} matching.
     * Once any of these methods is called it's allowed to define an alternative set of
     * {@link ReadingQualityType} related search criteria by calling {@link ReadingQualityWithTypeFilter#orOfAnotherType()}
     * or {@link ReadingQualityWithTypeFilter#orOfAnotherTypeInSameSystems()}
     *
     * @param indices {@link QualityCodeIndex QualityCodeIndices} to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * @return the self to proceed with search criteria definition or collect the results.
     */
    ReadingQualityWithTypeFilter ofQualityIndices(Set<QualityCodeIndex> indices);

    /**
     * Defines {@link QualityCodeCategory} and indices to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * Pls use exclusively with {@link #ofQualityIndex(QualityCodeIndex)},
     * {@link #ofQualityIndex(QualityCodeCategory, int)}, {@link #ofQualityIndices(Set)},
     * {@link #ofAnyQualityIndexInCategory(QualityCodeCategory)}, {@link #ofAnyQualityIndexInCategories(Set)}
     * because call of each one overrides previously defined criterion.
     * None of these methods called means any index in any {@link QualityCodeCategory} matching.
     * Once any of these methods is called it's allowed to define an alternative set of
     * {@link ReadingQualityType} related search criteria by calling {@link ReadingQualityWithTypeFilter#orOfAnotherType()}
     * or {@link ReadingQualityWithTypeFilter#orOfAnotherTypeInSameSystems()}
     *
     * @param category {@link QualityCodeCategory} to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * @param indices indices in <code>category</code> to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * @return the self to proceed with search criteria definition or collect the results.
     */
    ReadingQualityWithTypeFilter ofQualityIndices(QualityCodeCategory category, Set<Integer> indices);

    /**
     * Defines {@link QualityCodeCategory} to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * Pls use exclusively with {@link #ofQualityIndex(QualityCodeIndex)},
     * {@link #ofQualityIndex(QualityCodeCategory, int)}, {@link #ofQualityIndices(Set)},
     * {@link #ofQualityIndices(QualityCodeCategory, Set)}, {@link #ofAnyQualityIndexInCategories(Set)}
     * because call of each one overrides previously defined criterion.
     * None of these methods called means any index in any {@link QualityCodeCategory} matching.
     * Once any of these methods is called it's allowed to define an alternative set of
     * {@link ReadingQualityType} related search criteria by calling {@link ReadingQualityWithTypeFilter#orOfAnotherType()}
     * or {@link ReadingQualityWithTypeFilter#orOfAnotherTypeInSameSystems()}
     *
     * @param category {@link QualityCodeCategory} to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * @return the self to proceed with search criteria definition or collect the results.
     */
    ReadingQualityWithTypeFilter ofAnyQualityIndexInCategory(QualityCodeCategory category);

    /**
     * Defines {@link QualityCodeCategory QualityCodeCategories} to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * Pls use exclusively with {@link #ofQualityIndex(QualityCodeIndex)},
     * {@link #ofQualityIndex(QualityCodeCategory, int)}, {@link #ofQualityIndices(Set)},
     * {@link #ofQualityIndices(QualityCodeCategory, Set)}, {@link #ofAnyQualityIndexInCategory(QualityCodeCategory)}
     * because call of each one overrides previously defined criterion.
     * None of these methods called means any index in any {@link QualityCodeCategory} matching.
     * Once any of these methods is called it's allowed to define an alternative set of
     * {@link ReadingQualityType} related search criteria by calling {@link ReadingQualityWithTypeFilter#orOfAnotherType()}
     * or {@link ReadingQualityWithTypeFilter#orOfAnotherTypeInSameSystems()}
     *
     * @param categories {@link QualityCodeCategory QualityCodeCategories} to stick to when filtering by {@link ReadingQualityType}
     * during the search.
     * @return the self to proceed with search criteria definition or collect the results.
     */
    ReadingQualityWithTypeFilter ofAnyQualityIndexInCategories(Set<QualityCodeCategory> categories);
}
