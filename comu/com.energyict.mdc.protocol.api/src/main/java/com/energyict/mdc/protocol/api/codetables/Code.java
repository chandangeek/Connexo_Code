package com.energyict.mdc.protocol.api.codetables;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.NamedBusinessObject;

import java.time.Instant;
import java.util.List;
import java.util.TimeZone;

/**
 * Code implements a Calendar assigning to each interval a code.
 */
public interface Code extends NamedBusinessObject {

    /**
     * Returns the begin year of this Code
     *
     * @return the YearFrom of the Code
     */
    int getYearFrom();

    /**
     * Returns the end year of this Code
     *
     * @return the YearTo of the Code
     */
    int getYearTo();

    /**
     * Returns if the Code was verified
     *
     * @return Code was verified
     */
    boolean getVerified();

    /**
     * Returns if the Code was rebuilt
     *
     * @return Code was rebuilt
     */
    boolean getRebuilt();


    /**
     * Returns if dst is set to build the Code
     * KEPT FOR COMPATIBILITY WITH PREVIOUS VERSIONS OF EIServer
     * BUT IS NOW REPLACED BY TIMEZONE
     * <p/>
     * USE useDaylightTime TO DETERMINE IF THE RECEIVER'S TIME ZONE USES DST
     *
     * @return dst is set for the Code
     */
    boolean hasDst();

    /**
     * Returns the Code's interval in minutes
     *
     * @return the interval in minuts
     */
    int getIntervalInMinutes();

    /**
     * Returns the Code's interval in seconds
     *
     * @return the interval in seconds
     */
    int getIntervalInSeconds();

    /**
     * Returns the receiver's destination TimeZone.
     *
     * @return the receiver's destination TimeZone
     * @deprecated use getDestionationTimeZone
     */
    TimeZone getTimeZone();

    /**
     * Returns the receiver's destination TimeZone.
     * This is the timeZone used when creating the code table
     * in the database.
     *
     * @return the receiver's destination TimeZone
     */
    TimeZone getDestinationTimeZone();

    /**
     * Returns the receiver's definition TimeZone.
     * This is the timeZone used for interpreting the day types and calendar rules
     *
     * @return the receiver's definition TimeZone
     */
    TimeZone getDefinitionTimeZone();

    /**
     * Return the different codes defined for the Code table
     *
     * @return return the codes as an integer array
     */
    List<Integer> getCodes();

    /**
     * Return the different codes defined for the Code table valid for the given Interval
     *
     * @return return the codes as an integer array
     */
    List<Integer> getCodes(Interval period);

    /**
     * returns the day types for the receiver.
     *
     * @return a List of <Code>CodeDayType</Code> objects.
     */
    List<CodeDayType> getDayTypes();

    /**
     * returns the daytype with the given day type id
     *
     * @param dayTypeId the day type id to find
     * @return the day type or null.
     */
    CodeDayType getDayType(int dayTypeId);

    /**
     * return the list of calendar rules.
     *
     * @return a List of <Code>CodeCalendar</Code> objects.
     */
    List<CodeCalendar> getCalendars();

    /**
     * returns a list of dayTypes used within the given calendar rule
     *
     * @return a List of <Code>CodeDayType</Code> objects.
     */
    List<CodeDayType> getDayTypesOfCalendar();


    /**
     * returns the calendar rule with the given attributes.
     *
     * @param year      the year.
     * @param month     the month.
     * @param day       the day.
     * @param dayOfWeek the day of week.
     * @param dayType   the day type.
     * @return the calendar rule or null.
     */
    CodeCalendar getCodeCalendar(int year, int month, int day, int dayOfWeek, CodeDayType dayType);

    /**
     * returns the name of the codetable in the db
     *
     * @return the table name.
     * @deprecated
     */
    String getViewName();

    String getIntervalTable();

    /**
     * returns true if there are objects that make use of this Code
     *
     * @return true if in use, false otherwise.
     */
    boolean isInUse();

    /**
     * returns the code value for the specified date.
     *
     * @param date the date
     * @return the code.
     * @throws BusinessException if no code is available
     */
    int getCodeValue(Instant date) throws BusinessException;

    /**
     * returns the day type for the specified date.
     *
     * @param date the date
     * @return the day type
     * @throws BusinessException if no day type is available
     */
    CodeDayType getDayType(Instant date) throws BusinessException;

    /**
     * returns the period of the code: starting on the 1 januari of the code's begin year
     * and ending on the 31 december of the end year of the code.
     *
     * @return the code's <CODE>TimePeriod</CODE>.
     */
    Interval getPeriod();

    /**
     * Returns the receiver's Season set
     *
     * @return the receiver's season set
     */
    SeasonSet getSeasonSet();

    /**
     * Returns the id of the receiver's Season set
     *
     * @return the id of the receiver's season set
     */
    int getSeasonSetId();

    /**
     * returns the <Code>Season</Code> for the specified date.
     *
     * @param date the date
     * @return the season
     */
    Season getSeason(Instant date);

}