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

    public ReportInfo(YellowfinReportInfo reportInfo){
        this.category = reportInfo.getCategory();
        this.subCategory = reportInfo.getSubCategory();
        this.reportUUID = reportInfo.getReportUUID();
        this.description = reportInfo.getDescription();


    }

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
}
