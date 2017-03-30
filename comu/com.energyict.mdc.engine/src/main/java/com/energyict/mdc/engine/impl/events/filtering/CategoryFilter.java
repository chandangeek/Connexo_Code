/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.filtering;

import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.events.ComServerEvent;

import java.util.Set;

/**
 * Provides an implementation for the {@link EventFilterCriterion} interface
 * that will filter {@link ComServerEvent}s when they belong
 * to a certain set of {@link Category categories}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-31 (12:03)
 */
public class CategoryFilter implements EventFilterCriterion {

    private Set<Category> categories;

    public CategoryFilter (Set<Category> categories) {
        super();
        this.categories = categories;
    }

    @Override
    public boolean matches (ComServerEvent event) {
        return this.categories.contains(event.getCategory());
    }

    public Set<Category> getCategories () {
        return categories;
    }

    public void setCategories (Set<Category> categories) {
        this.categories = categories;
    }

}