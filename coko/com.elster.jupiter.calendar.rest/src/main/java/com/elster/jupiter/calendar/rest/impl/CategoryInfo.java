package com.elster.jupiter.calendar.rest.impl;

import com.elster.jupiter.calendar.Category;

public class CategoryInfo {

    public long id;
    public String name;
    public String displayName;

    public static CategoryInfo from(Category category) {
        CategoryInfo categoryInfo = new CategoryInfo();
        categoryInfo.id = category.getId();
        categoryInfo.name = category.getName();
        categoryInfo.displayName = category.getDisplayName();
        return categoryInfo;
    }
}
