/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.messages;

public enum DSTAlgorithm {

    once(0, "Repeated once exactly on day, month and year"),
    yearly(1, "Repeated each year on day and month"),
    yearlywd(2, "Repeated each year on \"a day in a week\"after \"a day\"and \"month\""),
    yearlyns(3, "Repeated each year on a day and month; if Sunday, it is moved to Monday"),
    yearlyaeast(4, "Repeated each year for X days after Easter (X =parameter \"day\" =0-255)"),
    yearlybeast(5, "Repeated each year for X days before Easter (X =parameter \"day\" =0-255)"),
    monthly(6, "Repeated each month on a certain day"),
    monthlywd(7, "Repeated each month on a day in a week after a certain day"),
    weekly(8, "Repeated each week on a day in a week"),
    daily(9, "Repeated each day");

    private final String description;
    private final int id;

    private DSTAlgorithm(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public static int fromDescription(String description) {
        for (DSTAlgorithm mode : values()) {
            if (mode.getDescription().equals(description)) {
                return mode.getId();
            }
        }
        return -1;
    }

    public static String[] getAllDescriptions() {
        String[] result = new String[values().length];
        for (int index = 0; index < values().length; index++) {
            result[index] = values()[index].getDescription();
        }
        return result;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }
}