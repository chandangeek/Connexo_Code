package com.elster.insight.usagepoint.data.rest.impl;

import java.util.List;
import java.util.Map;

public class UsagePointGroupInfo {

    public long id;
    public String mRID;
    public String name;
    public boolean dynamic;

    public Map<String, Object> filter; //frontend => backend (contains filter criteria for a dynamic group)
    public List<Long> usagePoints; //frontend => backend (contains selected devices ids for a static group)

//    public List<Long> deviceTypeIds = new ArrayList<>(); //backend => frontend
//    public List<SearchCriteriaInfo> criteria = new ArrayList<>(); //backend => frontend
//    public List<Long> deviceConfigurationIds = new ArrayList<>(); //backend => frontend

    public UsagePointGroupInfo() {
    }

}
