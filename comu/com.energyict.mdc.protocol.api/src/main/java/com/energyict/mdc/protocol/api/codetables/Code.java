package com.energyict.mdc.protocol.api.codetables;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.NamedBusinessObject;
import com.energyict.mdc.common.Protectable;
import com.energyict.mdc.dynamic.relation.RelationParticipant;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Code implements a Calendar assigning to each interval a code.
 */
public interface Code extends NamedBusinessObject, Protectable, RelationParticipant {

    /**
     * Returns the begin year of this Code
     *
     * @return the YearFrom of the Code
     */
    public int getYearFrom();

    /**
     * Returns the end year of this Code
     *
     * @return the YearTo of the Code
     */
    public int getYearTo();

    /**
     * Returns if the Code was verified
     *
     * @return Code was verified
     */
    public boolean getVerified();

    /**
     * Returns if the Code was rebuilt
     *
     * @return Code was rebuilt
     */
    public boolean getRebuilt();


    /**
     * Returns if dst is set to build the Code
     * KEPT FOR COMPATIBILITY WITH PREVIOUS VERSIONS OF EIServer
     * BUT IS NOW REPLACED BY TIMEZONE
     * <p/>
     * USE useDaylightTime TO DETERMINE IF THE RECEIVER'S TIME ZONE USES DST
     *
     * @return dst is set for the Code
     */
    public boolean hasDst();

    /**
     * Returns the Code's interval in minutes
     *
     * @return the interval in minuts
     */
    public int getIntervalInMinutes();

    /**
     * Returns the Code's interval in seconds
     *
     * @return the interval in seconds
     */
    public int getIntervalInSeconds();

    /**
     * Returns the receiver's destination TimeZone.
     *
     * @return the receiver's destination TimeZone
     * @deprecated use getDestionationTimeZone
     */
    public TimeZone getTimeZone();

    /**
     * Returns the receiver's destination TimeZone.
     * This is the timeZone used when creating the code table
     * in the database.
     *
     * @return the receiver's destination TimeZone
     */
    public TimeZone getDestinationTimeZone();

    /**
     * Returns the receiver's definition TimeZone.
     * This is the timeZone used for interpreting the day types and calendar rules
     *
     * @return the receiver's definition TimeZone
     */
    public TimeZone getDefinitionTimeZone();

    /**
     * Return the different codes defined for the Code table
     *
     * @return return the codes as an integer array
     */
    public List<Integer> getCodes();

    /**
     * Return the different codes defined for the Code table valid for the given Interval
     *
     * @return return the codes as an integer array
     */
    public List<Integer> getCodes(Interval period);

    /**
     * returns the day types for the receiver.
     *
     * @return a List of <Code>CodeDayType</Code> objects.
     */
    public List<CodeDayType> getDayTypes();

    /**
     * returns the daytype with the given day type id
     *
     * @param dayTypeId the day type id to find
     * @return the day type or null.
     */
    public CodeDayType getDayType(int dayTypeId);

    /**
     * return the list of calendar rules.
     *
     * @return a List of <Code>CodeCalendar</Code> objects.
     */
    public List<CodeCalendar> getCalendars();

    /**
     * returns a list of dayTypes used within the given calendar rule
     *
     * @return a List of <Code>CodeDayType</Code> objects.
     */
    public List<CodeDayType> getDayTypesOfCalendar();


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
    public CodeCalendar getCodeCalendar(int year, int month, int day, int dayOfWeek, CodeDayType dayType);

    /**
     * returns the name of the codetable in the db
     *
     * @return the table name.
     * @deprecated
     */
    public String getViewName();

    public String getIntervalTable();

    /**
     * returns true if there are objects that make use of this Code
     *
     * @return true if in use, false otherwise.
     */
    public boolean isInUse();

    /**
     * returns the code value for the specified date.
     *
     * @param date the date
     * @return the code.
     * @throws BusinessException if no code is available
     */
    public int getCodeValue(Date date) throws BusinessException;

    /**
     * returns the day type for the specified date.
     *
     * @param date the date
     * @return the day type
     * @throws BusinessException if no day type is available
     */
    public CodeDayType getDayType(Date date) throws BusinessException;

    /**
     * returns the period of the code: starting on the 1 januari of the code's begin year
     * and ending on the 31 december of the end year of the code.
     *
     * @return the code's <CODE>TimePeriod</CODE>.
     */
    public Interval getPeriod();

    /**
     * Returns the receiver's Season set
     *
     * @return the receiver's season set
     */
    public SeasonSet getSeasonSet();

    /**
     * Returns the id of the receiver's Season set
     *
     * @return the id of the receiver's season set
     */
    public int getSeasonSetId();

    /**
     * returns the <Code>Season</Code> for the specified date.
     *
     * @param date the date
     * @return the season
     */
    public Season getSeason(Date date);

}
