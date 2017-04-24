/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.tariff;//** Copyright Statement ***************************************************

import java.util.Calendar;

public class Holidays {

    public static Calendar getEasterMonday(int year) {
        int easterMonth = 0;
        int easterDay = 0;
        int march = 2; // March
        int april = 3; // April
        Calendar easterSunday = getEasterSunday(year);
        easterMonth = easterSunday.get(Calendar.MONTH);
        easterDay = easterSunday.get(Calendar.DAY_OF_MONTH);
        if (easterMonth == march || easterDay == 31) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(year, april, 1, 0, 0, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal;
        } else {
            Calendar cal = Calendar.getInstance();
            cal.set(year, easterMonth, ++easterDay, 0, 0, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal;
        }
    }

    public static Calendar getEasterSunday(int year) {
        int a, b, c, d, e, f, g, h, i, j, k, l, m;
        int easterMonth = 0;
        int easterDay = 0;

        if (year < 1900) {
            year += 1900;
        }

        a = year % 19;
        b = year / 100;
        c = year % 100;
        d = b / 4;
        e = b % 4;
        f = (b + 8) / 25;
        g = (b - f + 1) / 3;
        h = (19 * a + b - d - g + 15) % 30;
        i = c / 4;
        j = c % 4;
        k = (32 + 2 * e + 2 * i - h - j) % 7;
        l = (a + 11 * h + 22 * k) / 451;

        //  [3=March, 4=April]
        easterMonth = (h + k - 7 * l + 114) / 31;
        --easterMonth;
        m = (h + k - 7 * l + 114) % 31;

        // Date in Easter Month.
        easterDay = m + 1;

        // Uncorrect for our earlier correction.
        year -= 1900;

        // Populate the date object...
        Calendar cal = Calendar.getInstance();
        cal.set(year, easterMonth, easterDay, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

}
