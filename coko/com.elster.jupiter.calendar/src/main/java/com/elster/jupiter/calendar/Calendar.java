/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.time.Year;
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
 * a single DayType: holiday. All know holidays can be configured
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
}