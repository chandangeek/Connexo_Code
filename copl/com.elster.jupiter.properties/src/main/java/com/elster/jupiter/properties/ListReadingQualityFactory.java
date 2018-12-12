/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

/**
 * Provides support for multi-valued {@link PropertySpec}s.
 * <p>
 * Behaves exactly the same as the ListValueFactory, but the property type for the front end will be set to BasicPropertyTypes#LISTREADINGQUALITY.
 * The front end recognizes this value type and has a special editor for a list of reading qualities.
 *
 * @see PropertySpec#supportsMultiValues()
 * <p>
 */
public class ListReadingQualityFactory<T> extends ListValueFactory<T> {

    public ListReadingQualityFactory(ValueFactory<T> actualFactory, String separator) {
        super(actualFactory, separator);
    }
}