/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest;

/**
 * Created by bvn on 8/12/14.
 */
public class TaskStatusInfo {
    public String id;
    public String displayValue;

    public TaskStatusInfo() {
    }

    public TaskStatusInfo(String id, String displayValue) {
        this();
        this.id = id;
        this.displayValue = displayValue;
    }

}