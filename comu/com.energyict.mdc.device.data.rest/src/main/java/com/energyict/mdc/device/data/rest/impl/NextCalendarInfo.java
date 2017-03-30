/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

public class NextCalendarInfo {
    public String name;
    public long releaseDate;
    public long activationDate;
    public String status;
    public boolean willBePickedUpByPlannedComtask;
    public boolean willBePickedUpByComtask;

    public NextCalendarInfo(String name, long releaseDate, long activationDate, String status, boolean willBePickedUpByPlannedComtask, boolean willBePickedUpByComtask) {
        this.name = name;
        this.releaseDate = releaseDate;
        this.activationDate = activationDate;
        this.status = status;
        this.willBePickedUpByPlannedComtask = willBePickedUpByPlannedComtask;
        this.willBePickedUpByComtask = willBePickedUpByComtask;
    }
}
