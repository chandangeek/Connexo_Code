package com.energyict.mdc.tasks.rest.impl;

import com.energyict.mdc.tasks.rest.util.RestHelper;

import java.util.ArrayList;
import java.util.List;

public class CategoryInfo {
    public String id;
    public String name;

    public static CategoryInfo from(Categories category) {
        CategoryInfo categoryInfo = new CategoryInfo();
        RestHelper restHelper = new RestHelper();
        categoryInfo.id = category.getId();
        categoryInfo.name = restHelper.titleize(category.getId());
        return categoryInfo;
    }

    public static List<CategoryInfo> from(List<Categories> categories) {
        List<CategoryInfo> categoryInfos = new ArrayList<>(categories.size());
        for (Categories category : categories) {
            categoryInfos.add(CategoryInfo.from(category));
        }
        return categoryInfos;
    }
}