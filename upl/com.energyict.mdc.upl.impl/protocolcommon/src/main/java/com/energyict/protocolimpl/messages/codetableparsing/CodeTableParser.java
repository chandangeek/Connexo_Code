package com.energyict.protocolimpl.messages.codetableparsing;

import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.properties.TariffCalendar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converts a given {@link TariffCalendar} to easily usable objects for XML parsing.
 */
public class CodeTableParser {

    /**
     * The used CodeTable for this parser
     */
    private final TariffCalendar calender;
    private final Extractor extractor;

    /**
     * Defines a relation between the CalendarDayType Database ID and the ID that will be used in the xml file
     */
    private Map<String, Integer> tempDayIDMap = new HashMap<>();

    private Map<String, Integer> tempSeasonIdMap = new HashMap<>();

    /**
     * A Map containing all CalendarDayType ID's with there corresponding DayTypeDefinitions
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

    public CodeTableParser(TariffCalendar calender, Extractor extractor) {
        this.calender = calender;
        this.extractor = extractor;
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
        int counter = 0;
        for (Extractor.CalendarDayType dayType : this.extractor.dayTypes(this.calender)) {
            tempDayIDMap.put(dayType.id(), counter++);
        }
    }

    /**
     * Create a temporary Map for the SeasonId's, these should be unique in the Calendar, but using hte Database ID is not desirable
     */
    private void createTempSeasonIdMap() {
        Counter counter = new Counter();
        this.extractor
                .rules(this.calender)
                .stream()
                .filter(each -> each.seasonId().isPresent())
                .forEach(rule -> tempSeasonIdMap.computeIfAbsent(rule.seasonId().get(), seasonId -> counter.nextValue()));
    }

    /**
     * Get the dayID value for the given database dayId
     *
     * @param dbDayId the key to get the value from
     * @return the value from the {@link #tempDayIDMap} corresponding to the key
     */
    int getDayIDValue(String dbDayId) {
        return tempDayIDMap.get(dbDayId);
    }

    /**
     * Construct a list of {@link com.energyict.mdw.core.CodeDayType}s
     */
    private void constructDayProfileMap() {
        this.extractor
                .dayTypes(this.calender)
                .forEach(dayType ->
                        dayProfiles.put(
                                getDayIDValue(dayType.id()),
                                getDayTypeStartsFromCodeDayType(dayType)));
    }

    /**
     * Constructs a list of DayTypeDefinitions for the given CodeDayType
     *
     * @param dayType the CodeDayType to get the startDates from
     * @return the created list
     */
    private List<DayTypeDefinitions> getDayTypeStartsFromCodeDayType(Extractor.CalendarDayType dayType) {
        return dayType.slices().stream().map(DayTypeDefinitions::new).collect(Collectors.toList());
    }

    /**
     * Constructs a list of available seasons
     */
    private void constructSeasons() {
        this.extractor
                .rules(this.calender)
                .stream()
                .filter(each -> each.seasonId().isPresent())
                .filter(each -> tempSeasonIdMap.containsKey(each.seasonId().get()))
                .forEach(rule ->
                        seasonProfiles.put(
                                tempSeasonIdMap.get(rule.seasonId().get()),
                                new SeasonStartDates(rule)));
        checkStartTimes();
    }

    /**
     * Check whether the startDates of the seasons are correct. If only 1 season is defined, then it must start at a certain point,
     * not all fields may be empty, so just put in on 1th January.
     */
    private void checkStartTimes(){
        if (seasonProfiles.size() == 1){
            for (Integer key : seasonProfiles.keySet()) {
                SeasonStartDates newSeasonStartdate= new SeasonStartDates(-1, 1, 1);
                seasonProfiles.put(key, newSeasonStartdate);
            }
        }
    }

    /**
     * Constructs a list of defined weeks
     */
    private void constructWeeks() {
        for (int seasonId : seasonProfiles.keySet()) {
            weekProfiles.put(seasonId, getWeekDayTypes(seasonId));
            seasonProfiles.get(seasonId).setWeekProfileName(seasonId);
        }
    }

    /**
     * Constructs a list of WeekDayDefinitions for the given SeasonId
     *
     * @param seasonId the SeasonID to construct a list with
     * @return the created list
     */
    private List<WeekDayDefinitions> getWeekDayTypes(int seasonId) {
        return this.extractor
                    .rules(this.calender)
                    .stream()
                    .filter(each -> each.seasonId().isPresent())
                    .filter(each -> tempSeasonIdMap.containsKey(each.seasonId().get()))
                    .filter(rule -> tempSeasonIdMap.get(rule.seasonId().get()) == seasonId)
                    .map(rule -> new WeekDayDefinitions(this, rule))
                    .collect(Collectors.toList());
    }

    /**
     * Construct a list of SpecialDays
     */
    private void constructSpecialDays() {
        this.extractor
                .rules(this.calender)
                .stream()
                .filter(each -> !each.seasonId().isPresent())
                .map(rule -> new SpecialDayDefinition(this, rule))
                .forEach(this.specialDays::add);
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

    private static class Counter {
        private int next = 0;
        int nextValue() {
            return this.next++;
        }
    }
}