/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.din19244.poreg2.request.register;

import java.io.IOException;

public enum DaylightAlgorithm {
    once,
    yearly,
    yearlywd,
    yearlyns,
    yearlyaeast,
    yearlybeast,
    monthly,
    monthlywd,
    weekly,
    daily;

    public static DaylightAlgorithm valueFromOrdinal(int ordinal) throws IOException {
        for (DaylightAlgorithm algorithm : values()) {
            if (algorithm.ordinal() == ordinal) {
                return algorithm;
            }
        }
        throw new IOException("Failed to parse the DST settings: Invalid algorithm of repetition: " + ordinal + ".");
    }

    @Override
    public String toString() {
        switch (this.ordinal()) {
            case 0:
                return "repeated once exactly on day, month and year";
            case 1:
                return "repeated each year on day and month";
            case 2:
                return "repeated each year on \"a day in a week\"after \"a day\"and \"month\"";
            case 3:
                return "repeated each year on a day and month; if Sunday, it is moved to Monday";
            case 4:
                return "repeated each year for X days after Easter (X =parameter \"day\" =0-255)";
            case 5:
                return "repeated each year for X days before Easter (X =parameter \"day\" =0 -255)";
            case 6:
                return "repeated each month on a certain day";
            case 7:
                return "repeated each month on a day in a week after a certain day";
            case 8:
                return "repeated each week on a day in a week";
            case 9:
                return "repeated each day";
            default:
                return "Invalid state: " + this.ordinal();
        }
    }
}