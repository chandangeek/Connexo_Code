/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar;

import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.time.Year;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Optional;

/**
 * Models a timeline that can be configured to produce {@link Event}s
 * that occur on fixed points in time or on recurring points in time.
 * To facilitate the occurrence of the Events, you will configure
 * {@link DayType}s on which Events occur, divide the calendar
 * into {@link Period}s and specify the DayType for every
 * human calendar weekday. This provides a means to define
 * a standard week within that Period but there will obviously
 * be exceptions to these general rules or standards.
 * This is were {@link ExceptionalOccurrence}s come into play.
 * <p>
 * Calendars can extend another one, taking advantage of the occurrence
 * of events and map those events to others. In this way, it should
 * be possible to create a calendar that models the holidays
 * for the region where your system is operating in. Such a holiday
 * calendar would produce a single Event: holiday and also
 * a single DayType: holiday. All known holidays can be configured
 * to occur in this calendar. An extending calendar for e.g.
 * an electricity supplier that support off peak and on peak tariffs
 * could then map the holiday event to its own 'off peak' Event.
 * It suffices for an operator to add an additional holiday to the
 * base calendar and all extending calendars would be 'updated' as well.
 * Important note: the above feature of extension is not supported yet.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-07 (11:54)
 */
@ProviderType
public interface Calendar extends HasId, HasName {

    /**
     * Gets the Year on which this Calendar starts producing {@link Event}s.
     *
     * @return The start of this Calendar
     */
    Year getStartYear();

    /**
     * Gets the last Year during which this Calendar produces {@link Event}s.
     *
     * @return The start of this Calendar
     */
    Year getEndYear();

    /**
     * Gets the {@link Category} to which this Calendar belongs.
     *
     * @return The Category
     */
    Category getCategory();

    /**
     * The List of {@link Event}s that can occur in this Calendar.
     *
     * @return The List of Event
     */
    List<Event> getEvents();

    /**
     * The List of {@link Period}s that coincide with the human
     * calendar and that will specify how weekdays map onto {@link DayType}s.
     *
     * @return The List of Period
     */
    List<Period> getPeriods();

    /**
     * Gets the List of {@link PeriodTransitionSpec} that specify
     * when one {@link Period} transitions into another.
     * The List is ordered by time, i.e. the earliest transitions
     * are returned first.
     * Note also that all transitions must be of the same type.
     *
     * @return The List of PeriodTransitionSpec
     */
    List<? extends PeriodTransitionSpec> getPeriodTransitionSpecs();

    /**
     * Gets the List of {@link PeriodTransition} that specify
     * when one {@link Period} transitions into another.
     * The List is ordered by time, i.e. the earliest transitions
     * are returned first.
     * This is especially useful when the transitions of the Calendar
     * are using recurring dates. In that case all {@link PeriodTransitionSpec}s
     * will be calculated and converted to PeriodTransition starting at
     * the start year of this Calendar and running to the end of
     * the current year.
     *
     * @return The List of PeriodTransition
     */
    List<PeriodTransition> getTransitions();

    List<DayType> getDayTypes();

    List<ExceptionalOccurrence> getExceptionalOccurrences();

    String getDescription();

    long getVersion();

    Instant getCreateTime();

    Instant getModTime();

    String getUserName();

    String getMRID();

    void save();

    CalendarService.CalendarBuilder redefine();

    CalendarService.StrictCalendarBuilder update();

    boolean mayBeDeleted();

    void delete();

    boolean isAbstract();

    Status getStatus();

    String getStatusDisplayName();

    void deactivate();

    void activate();

    boolean isActive();

    /**
     * Makes this Calendar obsolete.
     * This Calendar will no longer show up in queries
     * except the one that is looking for a Calendar by its database id.
     */
    void makeObsolete();

    /**
     * The Instant in time when this Calendar was made obsolete.
     *
     * @return The instant in time or {@code Optional.empty()} if this Calendar is not obsolete
     */
    Optional<Instant> getObsoleteTime();

    /**
     * Returns a (persistent) TimeSeries that produces
     * event codes at the specified interval from the
     * {@link EventOccurrence}s and {@link ExceptionalOccurrence}s
     * specified by this Calendar.
     * <br>
     * Note that the TimeSeries is created lazily, i.e. the TimeSeries
     * will be created the first time it is requested and
     * the call to this method will need to run in a transaction
     * for that reason.
     * <br>
     * If the granularity of the EventOccurrences is smaller than
     * the requested interval then it is possible that some
     * EventOccurrences are lost in the resulting TimeSeries.
     * As an example, if all days of a Calendar are modelled as:
     * <table>
     * <tr><th>From</th><th>To</th><th>Event</th></tr>
     * <tr><td>00:00</td><td>07:00</td><td>Off peak</td></tr>
     * <tr><td>07:00</td><td>21:00</td><td>Peak</td></tr>
     * <tr><td>21:00</td><td>00:00</td><td>Off peak</td></tr>
     * </table>
     * an the Calendar is then converted to a TimeSeries producing
     * daily intervals then that TimeSeries will always produce
     * the code of the "Off peak" event as that is always the event
     * that is active at midnight and midnight is the only interval
     * that is produced for one day. The occurrence of the "Peak" event
     * and the switch to the second occurrence of the "Off peak" event
     * are lost that way.
     * Given that Calendars rely on {@link DayType}s
     * that are designed to have multiple {@link EventOccurrence},
     * it therefore does not really make sense to request
     * for a TimeSeries at an interval that equals or extends
     * the daily boundary but it is supported nevertheless.
     *
     * @param interval The interval
     * @param zoneId The ZoneId that will be used to convert the LocalTime information of the Calendar's EventOccurrences
     * @return The TimeSeries
     * @see TimeSeries#interval()
     */
    TimeSeries toTimeSeries(TemporalAmount interval, ZoneId zoneId);

}