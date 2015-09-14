package com.energyict.mdc.dashboard.rest.status.impl;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-04 (16:30)
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