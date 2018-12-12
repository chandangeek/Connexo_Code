/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.yellowfin;

/**
 * Created by Albertv on 12/3/2014.
 */
public interface YellowfinReportInfo {
    public int getReportId();
    public void setReportId(int reportId);

    public String getCategory();

    public void setCategory(String category);

    public String getSubCategory();

    public void setSubCategory(String subCategory);

    public String getReportUUID();

    public void setReportUUID(String reportUUID);

    public String getDescription();

    public void setDescription(String description);

    public void setName(String name);

    public String getName();
}
