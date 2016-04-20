package com.elster.jupiter.calendar;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;
import java.util.TimeZone;

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
     * Gets the TimeZone that was used to defined the occurrences
     * of the {@link Event}s. Note that this is mostly for
     * reference purposes. The occurrence of each Event
     * is calculated to UTC and should therefore no longer
     * need the TimeZone once the occurrences have been calculated.
     *
     * @return The TimeZone
     */
    TimeZone getTimeZone();

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

    void save();

    void delete();

    void setDescription(String description);

    DayType addDayType(String name);

    void removeDayType(DayType dayType);

    Period addPeriod(String name, DayType monday, DayType tuesday, DayType wednesday, DayType thursday, DayType friday, DayType saturday, DayType sunday);

    Event addEvent(String name, long code);

    void removePeriod(Period period);

    FixedExceptionalOccurrence addFixedExceptionalOccurrence(DayType dayType, int day, int month, int year);

    void removeFixedExceptionalOccurrence(FixedExceptionalOccurrence fixedExceptionalOccurrence);

    RecurrentExceptionalOccurrence addRecurrentExceptionalOccurrence(DayType dayType, int day, int month);

    void removeRecurrentExceptionalOccurrence(RecurrentExceptionalOccurrence recurrentExceptionalOccurrence);

    FixedPeriodTransitionSpec addFixedPeriodTransitionSpec(Period period, int day, int month, int year);

    void removeFixedPeriodTransitionSpec(FixedPeriodTransitionSpec fixedPeriodTransitionSpec);

    RecurrentPeriodTransitionSpec addRecurrentPeriodTransitionSpec(Period period, int day, int month);

    void removeRecurrentPeriodTransitionSpec(RecurrentPeriodTransitionSpec recurrentPeriodTransitionSpec);

    void removeEvent(Event event);

    List<DayType> getWeekTemplate(Instant time);
}