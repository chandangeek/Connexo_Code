/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share.entity;


public enum IssueTypes {

    DATA_COLLECTION("DCI", "datacollection"),
    DATA_VALIDATION("DVI", "datavalidation"),
    DEVICE_ALARM("ALM", "devicealarm"),
    DEVICE_LIFECYCLE("DLI", "devicelifecycle"),
    USAGEPOINT_DATA_VALIDATION("UVI", "usagepointdatavalidation"),
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
            case "usagepointdatavalidation" : return IssueTypes.USAGEPOINT_DATA_VALIDATION;
            default:return IssueTypes.NA;
        }
    }


    public static IssueTypes getByPrefix(String name){
        switch (name){
            case "ALM" : return IssueTypes.DEVICE_ALARM;
            case "DCI" : return IssueTypes.DATA_COLLECTION;
            case "DLI" : return IssueTypes.DEVICE_LIFECYCLE;
            case "DVI" : return IssueTypes.DATA_VALIDATION;
            case "UVI" : return IssueTypes.USAGEPOINT_DATA_VALIDATION;
            default:return IssueTypes.NA;
        }
    }
}
