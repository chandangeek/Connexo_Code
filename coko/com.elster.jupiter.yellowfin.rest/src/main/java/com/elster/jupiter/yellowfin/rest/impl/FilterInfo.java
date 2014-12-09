package com.elster.jupiter.yellowfin.rest.impl;

import com.elster.jupiter.yellowfin.YellowfinFilterInfo;

import java.math.BigDecimal;

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
    private boolean filterPrompt;
    private boolean filterAllowPrompt;
    private String filterDisplayName;
    private BigDecimal filterMaxValue;
    private BigDecimal filterMinValue;



    public FilterInfo(YellowfinFilterInfo filterInfo){
        this.id = filterInfo.getId();
        this.setFilterType(filterInfo.getFilterType());
        this.setFilterDisplayType(filterInfo.getFilterDisplayType());
        this.setFilterName(filterInfo.getFilterName());
        this.setFilterOmittable(filterInfo.isFilterOmittable());
        this.setFilterDataValues(filterInfo.getFilterDataValues());
        this.setFilterPrompt(filterInfo.isFilterPrompt());
        this.setFilterDisplayName(filterInfo.getFilterDisplayName());
        this.setFilterAllowPrompt(filterInfo.isFilterAllowPrompt());
        this.setFilterMinValue(filterInfo.getFilterMinValue());
        this.setFilterMaxValue(filterInfo.getFilterMaxValue());

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


    public boolean isFilterPrompt() {
        return filterPrompt;
    }

    public void setFilterPrompt(boolean filterPrompt) {
        this.filterPrompt = filterPrompt;
    }

    public boolean isFilterAllowPrompt() {
        return filterAllowPrompt;
    }

    public void setFilterAllowPrompt(boolean filterAllowPrompt) {
        this.filterAllowPrompt = filterAllowPrompt;
    }

    public String getFilterDisplayName() {
        return filterDisplayName;
    }

    public void setFilterDisplayName(String filterDisplayName) {
        this.filterDisplayName = filterDisplayName;
    }

    public BigDecimal getFilterMaxValue() {
        return filterMaxValue;
    }

    public void setFilterMaxValue(BigDecimal filterMaxValue) {
        this.filterMaxValue = filterMaxValue;
    }

    public BigDecimal getFilterMinValue() {
        return filterMinValue;
    }

    public void setFilterMinValue(BigDecimal filterMinValue) {
        this.filterMinValue = filterMinValue;
    }
}
