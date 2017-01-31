/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.yellowfin.rest.impl;

import com.elster.jupiter.yellowfin.YellowfinReportInfo;

/**
 * Created by Albertv on 12/3/2014.
 */
public class ReportInfo {
    private String category;
    private String subCategory;
    private String reportUUID;
    private String description;
    private String name;
    private int reportId;
    public ReportInfo(YellowfinReportInfo reportInfo){
        this.category = reportInfo.getCategory();
        this.subCategory = reportInfo.getSubCategory();
        this.reportUUID = reportInfo.getReportUUID();
        this.description = reportInfo.getDescription();
        this.name = reportInfo.getName();
        this.reportId = reportInfo.getReportId();


    }

    public int getReportId(){return  reportId;}

    public void setReportId(int reportId){this.reportId=reportId;}

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public String getReportUUID() {
        return reportUUID;
    }

    public void setReportUUID(String reportUUID) {
        this.reportUUID = reportUUID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
