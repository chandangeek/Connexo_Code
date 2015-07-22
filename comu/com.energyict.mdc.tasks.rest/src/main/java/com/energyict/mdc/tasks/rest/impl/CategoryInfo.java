package com.energyict.mdc.tasks.rest.impl;

import com.elster.jupiter.nls.Thesaurus;

import com.energyict.mdc.tasks.rest.Categories;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

public class CategoryInfo {

    private static final CategoriesAdapter categoriesAdapter = new CategoriesAdapter();
    @XmlJavaTypeAdapter(CategoriesAdapter.class)

    public String id;
    public String name;

    public static CategoryInfo from(Categories categories, Thesaurus thesaurus) {
        CategoryInfo categoryInfo = new CategoryInfo();
        if (categories!=null) {
            categoryInfo.id = categories.getId();
            String key = categoriesAdapter.marshal(categories);
            categoryInfo.name = thesaurus.getString(key, key);
        }
        return categoryInfo;
    }

    public static List<CategoryInfo> from(List<Categories> categories, Thesaurus thesaurus) {
        List<CategoryInfo> categoryInfos = new ArrayList<>(categories.size());
        for (Categories category : categories) {
            categoryInfos.add(CategoryInfo.from(category, thesaurus));
        }
        return categoryInfos;
    }
}