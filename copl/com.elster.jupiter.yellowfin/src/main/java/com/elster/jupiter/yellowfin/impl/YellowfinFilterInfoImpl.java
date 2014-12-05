package com.elster.jupiter.yellowfin.impl;

import com.elster.jupiter.yellowfin.YellowfinFilterInfo;

/**
 * Created by Albertv on 12/5/2014.
 */
public class YellowfinFilterInfoImpl implements YellowfinFilterInfo {
    private int id;
    private String filterType;
    private String filterDisplayType;
    private String filterName;
    private boolean filterOmittable;
    private String filterDataValues;


    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getFilterType() {
        return filterType;
    }

    @Override
    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    @Override
    public String getFilterDisplayType() {
        return filterDisplayType;
    }

    @Override
    public void setFilterDisplayType(String filterDisplayType) {
        this.filterDisplayType = filterDisplayType;
    }

    @Override
    public String getFilterName() {
        return filterName;
    }

    @Override
    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    @Override
    public boolean isFilterOmittable() {
        return filterOmittable;
    }

    @Override
    public void setFilterOmittable(boolean filterOmittable) {
        this.filterOmittable = filterOmittable;
    }

    @Override
    public String getFilterDataValues() {
        return filterDataValues;
    }

    @Override
    public void setFilterDataValues(String filterDataValues) {
        this.filterDataValues = filterDataValues;
    }
}
