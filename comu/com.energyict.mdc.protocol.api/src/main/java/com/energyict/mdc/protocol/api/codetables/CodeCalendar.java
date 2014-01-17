/*
 * CodeCalendar.java
 *
 * Created on 17 oktober 2003, 10:39
 */

package com.energyict.mdc.protocol.api.codetables;

import com.energyict.mdc.common.BusinessObject;

import java.util.Calendar;

/**
 * CodeCalendar represents a calendar rule.
 *
 * @author pasquien
 */
public interface CodeCalendar extends BusinessObject {

    /**
     * returns the year for which this rule can be applied.
     *
     * @return the year or -1 if any year
     */
    public int getYear();

    /**
     * returns the month for which the codecalendar is to be applied.
     *
     * @return the 1 based month index or -1 if any month
     */
    public int getMonth();

    /**
     * returns the day of month for which the codecalendar is to be applied.
     *
     * @return the 1 based month index or -1 if any month
     */
    public int getDay();

    /**
     * returns the day of the week for which this rule is applicable
     *
     * @return returns the day of week index ( monday = 1 , sunday = 7)
     *         or -1 if any day of week.
     */
    public int getDayOfWeek();


    /**
     * Get the code this rules belongs to
     *
     * @return the code
     */
    public Code getCode();

    /**
     * returns the day type to be used when the rule is matched.
     *
     * @return the day type
     */
    public CodeDayType getDayType();

    /**
     * Get the number of 'Any value' for the codeCalendar
     *
     * @return the number of any values.
     */
    public int getAnyCount();

    /**
     * checks if a date matches a codeCalendar
     *
     * @param cal the date to match
     * @return true if matched , false otherwhise
     */
    public boolean matches(Calendar cal);

    /**
     * Get a 'quotation' for the match of a date for a codeCalendar
     *
     * @param cal calendar to check.
     * @return 0 -> if the date fits completely : eg date 22/10/2003 fits the  CodeCalendar 2003 10 22 wednesday  completely
     *         1 - 4 -> the number of any's       eg date 22/10/2003 -> CodeCalendar  2003 10 any wednesday: returns 1
     *         eg date 22/10/2003 -> CodeCalendar  2003 10 any any: returns 2
     *         eg date 22/10/2003 -> CodeCalendar  2003 any any any: returns 3
     *         eg date 22/10/2003 -> CodeCalendar  any any any any: returns 4
     *         5  -> does not fit
     */
    public int getQuotation(Calendar cal);

    /**
     * When the 'quotation' we will use the codecalendar in next order
     *
     * @param cal calendar to check.
     * @return 1 : the codecalendar where the Day Of Week is defined ( not equal to any);
     *         2 : the codecalendar where the year is defined ( not equal to any);
     *         3 : the codecalendar where the month is defined ( not equal to any);
     *         4 : the codecalendar where the day is defined ( not equal to any);
     */
    public int getNaturalApplyOrder(Calendar cal);


    /**
     * returns the (id of the) season for which this rule can be applied
     *
     * @return the (id of the) season or -1 if any year
     */
    public int getSeason();

}
