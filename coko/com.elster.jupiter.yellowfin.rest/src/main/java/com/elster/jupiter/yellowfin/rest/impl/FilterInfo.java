/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
    private String filterDefaultValue1;
    private String filterDefaultValue2;
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
        this.setFilterDefaultValue1(filterInfo.getFilterDataValue1());
        this.setFilterDefaultValue2(filterInfo.getFilterDataValue2());
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

    public String getFilterDefaultValue1() {
        return filterDefaultValue1;
    }

    public void setFilterDefaultValue1(String filterDataValue1) {
        this.filterDefaultValue1 = filterDataValue1;
    }

    public String getFilterDefaultValue2() {
        return filterDefaultValue2;
    }

    public void setFilterDefaultValue2(String filterDataValue2) {
        this.filterDefaultValue2 = filterDataValue2;
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
