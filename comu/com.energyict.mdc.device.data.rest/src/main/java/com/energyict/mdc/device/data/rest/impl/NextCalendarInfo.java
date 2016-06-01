package com.energyict.mdc.device.data.rest.impl;

public class NextCalendarInfo {
    public String name;
    public long releaseDate;
    public long activationDate;
    public String status;

    public NextCalendarInfo(String name, long releaseDate, long activationDate, String status) {
        this.name = name;
        this.releaseDate = releaseDate;
        this.activationDate = activationDate;
        this.status = status;
    }
}
