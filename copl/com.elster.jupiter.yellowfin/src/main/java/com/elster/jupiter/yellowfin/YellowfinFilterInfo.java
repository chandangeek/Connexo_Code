package com.elster.jupiter.yellowfin;

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
    public String getFilterDataValues();
    public void setFilterDataValues(String filterDataValues);



}
