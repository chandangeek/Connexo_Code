package com.elster.jupiter.yellowfin.rest.impl;

import com.elster.jupiter.yellowfin.YellowfinFilterInfo;

/**
 * Created by Albertv on 12/5/2014.
 */
public class FilterInfo {
    private int id;
    private String filterType;
    private String filterDisplayType;
    private String filterName;
    private boolean filterOmittable;
    private String filterDataValues;


    public FilterInfo(YellowfinFilterInfo filterInfo){
        this.id = filterInfo.getId();
        this.setFilterType(filterInfo.getFilterType());
        this.setFilterDisplayType(filterInfo.getFilterDisplayType());
        this.setFilterName(filterInfo.getFilterName());
        this.setFilterOmittable(filterInfo.isFilterOmittable());
        this.setFilterDataValues(filterInfo.getFilterDataValues());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id= id;

    }

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    public String getFilterDisplayType() {
        return filterDisplayType;
    }

    public void setFilterDisplayType(String filterDisplayType) {
        this.filterDisplayType = filterDisplayType;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public boolean isFilterOmittable() {
        return filterOmittable;
    }

    public void setFilterOmittable(boolean filterOmittable) {
        this.filterOmittable = filterOmittable;
    }

    public String getFilterDataValues() {
        return filterDataValues;
    }

    public void setFilterDataValues(String filterDataValues) {
        this.filterDataValues = filterDataValues;
    }
}
