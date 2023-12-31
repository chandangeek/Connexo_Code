/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share.entity;


public enum IssueTypes {
    DATA_COLLECTION("DCI", "datacollection"),
    DATA_VALIDATION("DVI", "datavalidation"),
    DEVICE_ALARM("ALM", "devicealarm"),
    DEVICE_LIFECYCLE("DLI", "devicelifecycle"),
    TASK("TKI", "task"),
    USAGEPOINT_DATA_VALIDATION("UVI", "usagepointdatavalidation"),
    SERVICE_CALL_ISSUE("SCI", "servicecall"),
    MANUAL("ISU", "manual"),
    WEB_SERVICE("WSI", "webservice"),
    NA ("NA", "notapplicable");

    private final String prefix;
    private final String name;

    IssueTypes(String prefix, String name) {
        this.prefix = prefix;
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public String getPrefix(){
        return prefix;
    }

    public static IssueTypes getByName(String name){
        switch (name){
            case "datacollection" : return IssueTypes.DATA_COLLECTION;
            case "datavalidation" : return IssueTypes.DATA_VALIDATION;
            case "devicealarm" : return IssueTypes.DEVICE_ALARM;
            case "devicelifecycle" : return IssueTypes.DEVICE_LIFECYCLE;
            case "manual" : return IssueTypes.MANUAL;
            case "usagepointdatavalidation" : return IssueTypes.USAGEPOINT_DATA_VALIDATION;
            case "task" : return  IssueTypes.TASK;
            case "servicecall" : return IssueTypes.SERVICE_CALL_ISSUE;
            case "webservice" : return IssueTypes.WEB_SERVICE;
            default:return IssueTypes.NA;
        }
    }


    public static IssueTypes getByPrefix(String name){
        switch (name){
            case "ALM" : return IssueTypes.DEVICE_ALARM;
            case "DCI" : return IssueTypes.DATA_COLLECTION;
            case "DLI" : return IssueTypes.DEVICE_LIFECYCLE;
            case "DVI" : return IssueTypes.DATA_VALIDATION;
            case "TKI" : return IssueTypes.TASK;
            case "ISU" : return IssueTypes.MANUAL;
            case "UVI" : return IssueTypes.USAGEPOINT_DATA_VALIDATION;
            case "SCI" : return IssueTypes.SERVICE_CALL_ISSUE;
            case "WSI" : return IssueTypes.WEB_SERVICE;
            default:return IssueTypes.NA;
        }
    }
}
