package com.energyict.protocolimpl.messages.codetableparsing;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.util.HasId;

import com.energyict.protocolimpl.generic.messages.ArrayIndexGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converts a given {@link Calendar} to easily usable objects for XML parsing
 */
public class CodeTableParser {

    /**
     * The Calendar used for parsing.
     */
    private final Calendar calendar;

    /**
     * Defines a relation between the DayType Database ID and the ID that will be used in the xml file
     */
    private Map<Long, Integer> tempDayIDMap = new HashMap<>();

    private Map<Long, Integer> tempSeasonIdMap = new HashMap<>();

    /**
     * A Map containing all DayType ID's with there corresponding DayTypeDefinitions
     */
    private Map<Integer, List<DayTypeDefinitions>> dayProfiles = new HashMap<>();

    /**
     * A Map containing all Season ID's with there corresponding SeasonStartDate
     */
    private Map<Integer, SeasonStartDates> seasonProfiles = new HashMap<>();

    /**
     * A Map containing all the Week ID's (the same as the Season ID's) with there corresponding list of WeekDayDefinitions
     */
    private Map<Integer, List<WeekDayDefinitions>> weekProfiles = new HashMap<>();

    /**
     * A List containing all the SpecialDays
     */
    private List<SpecialDayDefinition> specialDays = new ArrayList<>();

    public CodeTableParser(Calendar calendar) {
        this.calendar = calendar;
    }

    /**
     * Parsing of the codeTable to season-, week- and dayProfiles
     */
    public void parse() {
        createTempDayIDMap();
        createTempSeasonIdMap();

        // Create a map of available dayTypes
        constructDayProfileMap();

        // Create a map of SeasonProfileStartDates
        constructSeasons();

        // Create a map of the WeekProfiles
        constructWeeks();

        // Create a map of SpecialDays
        constructSpecialDays();
    }

    /**
     * Create a temporary Map for the DayID's, these should be unique in the calendar, but using the Database ID is not desirable
     */
    private void createTempDayIDMap() {
        this.tempDayIDMap = this.buildIdToArrayIndexMap(this.calendar.getDayTypes());
    }

    /**
     * Create a temporary Map for the SeasonId's, these should be unique in the Calendar, but using the Database ID is not desirable
     */
    private void createTempSeasonIdMap() {
        this.tempSeasonIdMap = this.buildIdToArrayIndexMap(this.calendar.getPeriods());
    }

    private Map<Long, Integer> buildIdToArrayIndexMap(List<? extends HasId> withIds) {
        final ArrayIndexGenerator counter = ArrayIndexGenerator.zeroBased();
        return withIds
                .stream()
                .collect(Collectors.toMap(
                        HasId::getId,
                        dt -> counter.next()));
    }

    /**
     * Get the dayID value for the given database dayId
     *
     * @param dbDayId the key to get the value from
     * @return the value from the {@link #tempDayIDMap} corresponding to the key
     */
    int getDayIDValue(long dbDayId) {
        return tempDayIDMap.get(dbDayId);
    }

    private void constructDayProfileMap() {
        this.dayProfiles =
                this.calendar
                        .getDayTypes()
                        .stream()
                        .collect(Collectors.toMap(
                                dayType -> this.getDayIDValue(dayType.getId()),
                                this::getDayTypeStartsFromCodeDayType));
    }

    /**
     * Constructs a list of DayTypeDefinitions for the given CodeDayType
     *
     * @param dayType the CodeDayType to get the startDates from
     * @return the created list
     */
    private List<DayTypeDefinitions> getDayTypeStartsFromCodeDayType(DayType dayType) {
        return dayType.getEventOccurrences().stream().map(DayTypeDefinitions::new).collect(Collectors.toList());
    }

    /**
     * Constructs a list of available seasons
     */
    private void constructSeasons() {
        this.seasonProfiles =
                this.calendar
                        .getPeriods()
                        .stream()
                        .collect(Collectors.toMap(
                            period -> this.tempSeasonIdMap.get(period.getId()),
                            SeasonStartDates::new));
    }

    /**
     * Constructs a list of defined weeks
     */
    private void constructWeeks() {
        for (int seasonIndex : seasonProfiles.keySet()) {
            weekProfiles.put(seasonIndex, getWeekDayTypes(seasonIndex));
            seasonProfiles.get(seasonIndex).setWeekProfileName(seasonIndex);
        }
    }

    /**
     * Constructs a list of WeekDayDefinitions for the given SeasonId
     *
     * @param seasonIndex the SeasonIndex to construct a list with
     * @return the created list
     */
    private List<WeekDayDefinitions> getWeekDayTypes(int seasonIndex) {
        return this.calendar
                .getPeriods()
                .stream()
                .filter(period -> this.tempSeasonIdMap.get(period.getId()) == seasonIndex)
                .map(period -> WeekDayDefinitions.fromPeriod(period, this))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Construct a list of SpecialDays
     */
    private void constructSpecialDays() {
        this.specialDays =
                this.calendar
                        .getExceptionalOccurrences()
                        .stream()
                        .map(exceptionalOccurrence -> new SpecialDayDefinition(this, exceptionalOccurrence))
                        .collect(Collectors.toList());
    }

    /**
     * Getter for the DayProfiles
     *
     * @return the {@link #dayProfiles}
     */
    public Map<Integer, List<DayTypeDefinitions>> getDayProfiles() {
        return dayProfiles;
    }

    /**
     * Getter for the SeasonProfiles
     *
     * @return the {@link #seasonProfiles}
     */
    public Map<Integer, SeasonStartDates> getSeasonProfiles() {
        return seasonProfiles;
    }

    /**
     * Getter for the WeekProfiles
     *
     * @return the {@link #weekProfiles}
     */
    public Map<Integer, List<WeekDayDefinitions>> getWeekProfiles() {
        return weekProfiles;
    }

    /**
     * Getter for the SpecialDays
     *
     * @return the {@link #specialDays}
     */
    public List<SpecialDayDefinition> getSpecialDaysProfile() {
        return specialDays;
    }

}

