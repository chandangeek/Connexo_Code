/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Passes constrictions of one constraining {@link SearchableProperty}
 * to a dependent SearchableProperty.
 * @see SearchableProperty#getConstraints()
 * @see SearchableProperty#getConstraints()
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-01 (10:41)
 */
public final class SearchablePropertyConstriction {

    private final SearchableProperty constrainingProperty;
    private final List<Object> constrainingValues;

    public static SearchablePropertyConstriction noValues(SearchableProperty constrainingProperty) {
        return new SearchablePropertyConstriction(constrainingProperty, Collections.emptyList());
    }

    public static SearchablePropertyConstriction withValues(SearchableProperty constrainingProperty, Object... constrainingValues) {
        return withValues(constrainingProperty, Arrays.asList(constrainingValues));
    }

    public static SearchablePropertyConstriction withValues(SearchableProperty constrainingProperty, List<Object> constrainingValues) {
        return new SearchablePropertyConstriction(constrainingProperty, Collections.unmodifiableList(constrainingValues));
    }

    private SearchablePropertyConstriction(SearchableProperty constrainingProperty, List<Object> constrainingValues) {
        this.constrainingProperty = constrainingProperty;
        this.constrainingValues = constrainingValues;
    }

    public SearchableProperty getConstrainingProperty() {
        return constrainingProperty;
    }

    public List<Object> getConstrainingValues() {
        return constrainingValues;
    }

}