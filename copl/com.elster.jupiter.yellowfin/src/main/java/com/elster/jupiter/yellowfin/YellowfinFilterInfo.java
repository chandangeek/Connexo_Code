/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.yellowfin;

import java.math.BigDecimal;

/**
 * Created by Albertv on 12/5/2014.
 */
public interface YellowfinFilterInfo {
    public int getId();
    public void setId(int id);
    public String getFilterType();
    public void setFilterType(String filterType);
    public String getFilterDisplayType();
    public void setFilterDisplayType(String filterDisplayType);
    public String getFilterName();
    public void setFilterName(String filterName);
    public boolean isFilterOmittable();
    public void setFilterOmittable(boolean filterOmittable);
    public String getFilterDataValue1();
    public String getFilterDataValue2();
    public void setFilterDataValue1(String filterDataValue1);
    public void setFilterDataValue2(String filterDataValue2);

    public boolean isFilterPrompt();
    public void setFilterPrompt(boolean filterPrompt);
    public String getFilterDisplayName() ;
    public void setFilterDisplayName(String filterDisplayName) ;
    public boolean isFilterAllowPrompt();
    public void setFilterAllowPrompt(boolean filterAllowPrompt);
    public BigDecimal getFilterMinValue();
    public void setFilterMinValue(BigDecimal filterMinValue);
    public BigDecimal getFilterMaxValue();
    public void setFilterMaxValue(BigDecimal filterMaxValue);



}
