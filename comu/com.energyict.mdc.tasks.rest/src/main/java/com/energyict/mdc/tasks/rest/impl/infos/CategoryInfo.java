package com.energyict.mdc.tasks.rest.impl.infos;

import com.energyict.mdc.tasks.rest.impl.Categories;
import com.energyict.mdc.tasks.rest.util.RestHelper;

import java.util.ArrayList;
import java.util.List;

public class CategoryInfo {
    private String id;
    private String name;

    public static CategoryInfo from(Categories category) {
        CategoryInfo categoryInfo = new CategoryInfo();
        categoryInfo.setId(category.getId());
        categoryInfo.setName(RestHelper.titleize(category.getId()));
        return categoryInfo;
    }

    public static List<CategoryInfo> from(List<Categories> categories) {
        List<CategoryInfo> categoryInfos = new ArrayList<>(categories.size());
        for (Categories category : categories) {
            categoryInfos.add(CategoryInfo.from(category));
        }
        return categoryInfos;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}