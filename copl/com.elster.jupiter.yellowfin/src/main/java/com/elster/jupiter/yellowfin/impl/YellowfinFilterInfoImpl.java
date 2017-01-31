/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.yellowfin.impl;

import com.elster.jupiter.yellowfin.YellowfinFilterInfo;

import java.math.BigDecimal;

/**
 * Created by Albertv on 12/5/2014.
 */
public class YellowfinFilterInfoImpl implements YellowfinFilterInfo {
    private int id;
    private String filterType;
    private String filterDisplayType;
    private String filterName;
    private boolean filterOmittable;
    private String filterDataValue1;
    private String filterDataValue2;
    private boolean filterPrompt;
    private String filterDisplayName;
    private boolean filterAllowPrompt;
    private BigDecimal filterMinValue;
    private BigDecimal filterMaxValue;



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
    public String getFilterDataValue1() {
        return this.filterDataValue1;
    }

    @Override
    public String getFilterDataValue2() {
        return this.filterDataValue2;
    }

    @Override
    public void setFilterDataValue1(String filterDataValue1) {
        this.filterDataValue1 = filterDataValue1;
    }

    @Override
    public void setFilterDataValue2(String filterDataValue2) {
        this.filterDataValue2 = filterDataValue2;
    }


    public boolean isFilterPrompt() {
        return filterPrompt;
    }

    public void setFilterPrompt(boolean filterPrompt) {
        this.filterPrompt = filterPrompt;
    }

    public String getFilterDisplayName() {
        return filterDisplayName;
    }

    public void setFilterDisplayName(String filterDisplayName) {
        this.filterDisplayName = filterDisplayName;
    }

    public boolean isFilterAllowPrompt() {
        return filterAllowPrompt;
    }

    public void setFilterAllowPrompt(boolean filterAllowPrompt) {
        this.filterAllowPrompt = filterAllowPrompt;
    }

    public BigDecimal getFilterMinValue() {
        return filterMinValue;
    }

    public void setFilterMinValue(BigDecimal filterMinValue) {
        this.filterMinValue = filterMinValue;
    }

    public BigDecimal getFilterMaxValue() {
        return filterMaxValue;
    }

    public void setFilterMaxValue(BigDecimal filterMaxValue) {
        this.filterMaxValue = filterMaxValue;
    }
}
