package com.energyict.protocolimpl.messages.codetableparsing;

import com.energyict.mdc.upl.properties.TariffCalender;

import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.CodeCalendar;
import com.energyict.mdw.core.CodeDayType;
import com.energyict.mdw.core.CodeDayTypeDef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts a given {@link Code} to easily usable objects for XML parsing
 */
public class CodeTableParser {

    /**
     * The used CodeTable for this parser
     */
    private final TariffCalender calender;

    /**
     * Defines a relation between the DayType Database ID and the ID that will be used in the xml file
     */
    private Map<Integer, Integer> tempDayIDMap = new HashMap<Integer, Integer>();

    private Map<Integer, Integer> tempSeasonIdMap = new HashMap<Integer, Integer>();

    /**
     * A Map containing all DayType ID's with there corresponding DayTypeDefinitions
     */
    private Map<Integer, List<DayTypeDefinitions>> dayProfiles = new HashMap<Integer, List<DayTypeDefinitions>>();

    /**
     * A Map containing all Season ID's with there corresponding SeasonStartDate
     */
    private Map<Integer, SeasonStartDates> seasonProfiles = new HashMap<Integer, SeasonStartDates>();

    /**
     * A Map containing all the Week ID's (the same as the Season ID's) with there corresponding list of WeekDayDefinitions
     */
    private Map<Integer, List<WeekDayDefinitions>> weekProfiles = new HashMap<Integer, List<WeekDayDefinitions>>();

    /**
     * A List containing all the SpecialDays
     */
    private List<SpecialDayDefinition> specialDays = new ArrayList<SpecialDayDefinition>();


    /**
     * Default constructor
     */
    public CodeTableParser(TariffCalender calender) {
        this.calender = calender;
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
        for (CodeDayType cdt : calender.getDayTypes()) {
            tempDayIDMap.put(cdt.getId(), counter++);
        }
    }

    /**
     * Create a temporary Map for the SeasonId's, these should be unique in the Calendar, but using hte Database ID is not desirable
     */
    private void createTempSeasonIdMap(){
        int counter = 0;
        for(CodeCalendar cCalendars : calender.getCalendars()){
            if(!tempSeasonIdMap.containsKey(cCalendars.getSeason()) && (cCalendars.getSeason() != 0)){
                tempSeasonIdMap.put(cCalendars.getSeason(), counter++);
            }
        }
    }

    /**
     * Get the dayID value for the given database dayId
     *
     * @param dbDayId the key to get the value from
     * @return the value from the {@link #tempDayIDMap} corresponding to the key
     */
    int getDayIDValue(int dbDayId) {
        return tempDayIDMap.get(dbDayId);
    }

    /**
     * Construct a list of {@link com.energyict.mdw.core.CodeDayType}s
     */
    private final void constructDayProfileMap() {
        for (CodeDayType cdt : calender.getDayTypes()) {
            dayProfiles.put(getDayIDValue(cdt.getId()), getDayTypeStartsFromCodeDayType(cdt));
        }
    }

    /**
     * Constructs a list of DayTypeDefinitions for the given CodeDayType
     *
     * @param cdt the CodeDayType to get the startDates from
     * @return the created list
     */
    private final List<DayTypeDefinitions> getDayTypeStartsFromCodeDayType(CodeDayType cdt) {
        List<DayTypeDefinitions> startDates = new ArrayList<DayTypeDefinitions>();
        for (Object cdtd : cdt.getDefinitions()) {
            startDates.add(new DayTypeDefinitions((CodeDayTypeDef) cdtd));
        }
        return startDates;
    }

    /**
     * Constructs a list of available seasons
     */
    private final void constructSeasons() {
        List<com.energyict.mdw.core.CodeCalendar> calendars = calender.getCalendars();

        for (CodeCalendar cc : calendars) {
            if(tempSeasonIdMap.get(cc.getSeason()) != null){
                seasonProfiles.put(tempSeasonIdMap.get(cc.getSeason()), new SeasonStartDates(cc));
            }
        }

        checkStartTimes();
    }

    /**
     * Check whether the startDates of the seasons are correct. If only 1 season is defined, then it must start at a certain point,
     * not all fields may be empty, so just put in on 1th January.
     */
    private final void checkStartTimes(){
        if(seasonProfiles.size() == 1){
            for(Integer key : seasonProfiles.keySet()){
                SeasonStartDates newSeasonStartdate= new SeasonStartDates(-1, 1, 1);
                seasonProfiles.put(key, newSeasonStartdate);
            }
        }
    }

    /**
     * Constructs a list of defined weeks
     */
    private final void constructWeeks() {
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
        List<WeekDayDefinitions> weekDayDefs = new ArrayList<WeekDayDefinitions>();
        for (CodeCalendar cc : calender.getCalendars()) {
            if(tempSeasonIdMap.containsKey(cc.getSeason())){
                if (tempSeasonIdMap.get(cc.getSeason()) == seasonId) {
                    weekDayDefs.add(new WeekDayDefinitions(this, cc));
                }
            }
        }
        return weekDayDefs;
    }

    /**
     * Construct a list of SpecialDays
     */
    private final void constructSpecialDays() {
        for (CodeCalendar cc : calender.getCalendars()) {
            if (cc.getSeason() == 0) { // '0' means no Calendar is defined so it is a special day
                specialDays.add(new SpecialDayDefinition(this, cc));
            }
        }
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

