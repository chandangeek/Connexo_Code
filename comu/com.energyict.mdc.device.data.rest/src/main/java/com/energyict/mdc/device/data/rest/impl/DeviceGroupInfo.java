package com.energyict.mdc.device.data.rest.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeviceGroupInfo {

    public long id;
    public String mRID;
    public String name;
    public boolean dynamic;

    public Map<String, Object> filter; //frontend => backend (contains filter criteria for a dynamic group)
    public List<Long> devices; //frontend => backend (contains selected devices ids for a static group)

    public List<Long> deviceTypeIds = new ArrayList<>(); //backend => frontend
    public List<SearchCriteriaInfo> criteria = new ArrayList<>(); //backend => frontend
    public List<Long> deviceConfigurationIds = new ArrayList<>(); //backend => frontend

    public long version;

    public DeviceGroupInfo() {
    }

}
