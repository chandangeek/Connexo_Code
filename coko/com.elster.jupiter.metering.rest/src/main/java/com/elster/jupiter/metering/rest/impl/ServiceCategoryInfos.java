package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.ServiceCategory;

import java.util.ArrayList;
import java.util.List;

public class ServiceCategoryInfos {

    public int total = 0;
    public List<ServiceCategoryInfo> categories = new ArrayList<>();

    public ServiceCategoryInfos(List<ServiceCategory> serviceCategories) {
        for (ServiceCategory category : serviceCategories) {
            categories.add(new ServiceCategoryInfo(category));
            total++;
        }
    }

    public static class ServiceCategoryInfo {
        public String name;
        public String displayName;

        public ServiceCategoryInfo(ServiceCategory category) {
            this.name = category.getKind().name();
            this.displayName = category.getName();
        }
    }
}
