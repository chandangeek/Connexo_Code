/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share.entity;


public enum IssueTypes {

    DATA_COLLECTION("DCI", "datacollection"),
    DATA_VALIDATION("DVI", "datavalidation"),
    DEVICE_ALARM("ALM", "devicealarm"),
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
            default:return IssueTypes.NA;
        }
    }


    public static IssueTypes getByPrefix(String name){
        switch (name){
            case "DCI" : return IssueTypes.DATA_COLLECTION;
            case "DVI" : return IssueTypes.DATA_VALIDATION;
            case "ALM" : return IssueTypes.DEVICE_ALARM;
            default:return IssueTypes.NA;
        }
    }
}
