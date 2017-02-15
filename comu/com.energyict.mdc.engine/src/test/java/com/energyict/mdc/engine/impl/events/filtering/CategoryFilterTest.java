/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.filtering;

import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.events.ComServerEvent;

import java.util.EnumSet;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.events.filtering.CategoryFilter} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-31 (12:06)
 */
public class CategoryFilterTest {

    @Test
    public void testMatch () {
        CategoryFilter filter = new CategoryFilter(EnumSet.allOf(Category.class));
        ComServerEvent event = mock(ComServerEvent.class);
        when(event.getCategory()).thenReturn(Category.CONNECTION);

        // Business method and assert
        assertThat(filter.matches(event)).isTrue();
    }

    @Test
    public void testNoMatch () {
        CategoryFilter filter = new CategoryFilter(EnumSet.noneOf(Category.class));
        ComServerEvent event = mock(ComServerEvent.class);
        when(event.getCategory()).thenReturn(Category.CONNECTION);

        // Business method and assert
        assertThat(filter.matches(event)).isFalse();
    }

    @Test
    public void testConstructor () {
        // Business method
        CategoryFilter filter = new CategoryFilter(EnumSet.of(Category.COLLECTED_DATA_PROCESSING, Category.LOGGING));

        // Asserts
        assertThat(filter.getCategories()).containsOnly(Category.COLLECTED_DATA_PROCESSING, Category.LOGGING);
    }

    @Test
    public void testUpdateCategories () {
        CategoryFilter filter = new CategoryFilter(EnumSet.of(Category.COLLECTED_DATA_PROCESSING, Category.LOGGING));

        // Business method
        filter.setCategories(EnumSet.of(Category.CONNECTION, Category.COMTASK));

        // Asserts
        assertThat(filter.getCategories()).containsOnly(Category.CONNECTION, Category.COMTASK);
    }

}