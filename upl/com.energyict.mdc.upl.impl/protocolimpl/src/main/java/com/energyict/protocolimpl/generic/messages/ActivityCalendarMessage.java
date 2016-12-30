package com.energyict.protocolimpl.generic.messages;

import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.properties.TariffCalendar;

import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.attributeobjects.DayProfileActions;
import com.energyict.dlms.cosem.attributeobjects.DayProfiles;
import com.energyict.dlms.cosem.attributeobjects.SeasonProfiles;
import com.energyict.dlms.cosem.attributeobjects.WeekProfiles;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NotInObjectListException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ActivityCalendarMessage {

    private final TariffCalendar calendar;
    private final Extractor extractor;
    private final DLMSMeterConfig meterConfig;
    private Array dayArray;
    private Array seasonArray;
    private Array weekArray;
    protected Map<String, Integer> seasonIds = new HashMap<>();  //Map the DB id's of the seasons to a proper 0-based index that can be used in the AXDR array
    protected Map<String, Integer> dayTypeIds = new HashMap<>();  //Map the DB id's of the day types to a proper 0-based index that can be used in the AXDR array

    public ActivityCalendarMessage(TariffCalendar calendar, Extractor extractor, DLMSMeterConfig meterConfig) {
        this.calendar = calendar;
        this.extractor = extractor;
        this.meterConfig = meterConfig;
        this.dayArray = new Array();
        this.seasonArray = new Array();
        this.weekArray = new Array();
        this.seasonIds = new HashMap<>();
        this.dayTypeIds = new HashMap<>();
    }

    /**
     * Parsing of the codeTable to season-, week- and dayProfiles
     */
    public void parse() throws NotInObjectListException {
        List<Extractor.CalendarRule> rules = extractor.rules(calendar);
        Map<OctetString, String> seasonsProfile = new HashMap<>();

        Iterator<Extractor.CalendarRule> itr = rules.iterator();
        int index = 0;
        while (itr.hasNext()) {
            Extractor.CalendarRule cc = itr.next();
            String seasonId = cc.seasonId().orElse("");
            if (cc.seasonId().isPresent()) {
                OctetString os = OctetString.fromByteArray(new byte[]{(byte) ((cc.year() == -1) ? 0xff : ((cc.year() >> 8) & 0xFF)), (byte) ((cc.year() == -1) ? 0xff : (cc.year()) & 0xFF),
                        (byte) ((cc.month() == -1) ? 0xFF : cc.month()), (byte) ((cc.day() == -1) ? 0xFF : cc.day()), (byte) 0xFF, 0, 0, 0, 0, (byte) 0x80, 0, 0});
                seasonsProfile.put(os, seasonId);
                if (!seasonIds.containsKey(seasonId)) {
                    seasonIds.put(seasonId, index);
                    index++;
                }
            }
        }

        //Create day type IDs (incremental 0-based)
        List<Extractor.CalendarDayType> calendarDayTypes = extractor.dayTypes(calendar);
        for (int dayTypeIndex = 0; dayTypeIndex < calendarDayTypes.size(); dayTypeIndex++) {
            Extractor.CalendarDayType dayType = calendarDayTypes.get(dayTypeIndex);
            if (!dayTypeIds.containsKey(dayType.id())) {
                dayTypeIds.put(dayType.id(), dayTypeIndex);
            }
        }

        int weekIndex = 0;
        for (Map.Entry<OctetString, String> entry : seasonsProfile.entrySet()) {

            String seasonProfileNameId = getSeasonProfileName(entry);
            if (!seasonArrayExists(seasonProfileNameId, seasonArray)) {
                int weekProfileName = weekIndex;
                weekIndex++;
                SeasonProfiles seasonProfiles = new SeasonProfiles();
                seasonProfiles.setSeasonProfileName(OctetString.fromString(seasonProfileNameId));    // the seasonProfileName is the DB id of the season
                seasonProfiles.setSeasonStart(entry.getKey());
                seasonProfiles.setWeekName(getOctetStringFromInt(weekProfileName));
                seasonArray.addDataType(seasonProfiles);
                if (!weekArrayExists(weekProfileName, weekArray)) {
                    WeekProfiles wp = new WeekProfiles();
                    Iterator<Extractor.CalendarRule> sIt = rules.iterator();
                    String dayTypeIds[] = {null, null, null, null, null, null, null};
                    String any = null;
                    while (sIt.hasNext()) {
                        Extractor.CalendarRule rule = sIt.next();
                        if (rule.seasonId().orElse("").equals(entry.getValue())) {
                            switch (rule.dayOfWeek()) {
                                case 1: {
                                    if (dayTypeIds[0] != null) {
                                        if (dayTypeIds[0] != rule.dayTypeId()) {
                                            throw new IllegalArgumentException("Season profiles are not correctly configured.");
                                        }
                                    } else {
                                        dayTypeIds[0] = rule.dayTypeId();
                                    }
                                }
                                break;
                                case 2: {
                                    if (dayTypeIds[1] != null) {
                                        if (dayTypeIds[1] != rule.dayTypeId()) {
                                            throw new IllegalArgumentException("Season profiles are not correctly configured.");
                                        }
                                    } else {
                                        dayTypeIds[1] = rule.dayTypeId();
                                    }
                                }
                                break;
                                case 3: {
                                    if (dayTypeIds[2] != null) {
                                        if (dayTypeIds[2] != rule.dayTypeId()) {
                                            throw new IllegalArgumentException("Season profiles are not correctly configured.");
                                        }
                                    } else {
                                        dayTypeIds[2] = rule.dayTypeId();
                                    }
                                }
                                break;
                                case 4: {
                                    if (dayTypeIds[3] != null) {
                                        if (dayTypeIds[3] != rule.dayTypeId()) {
                                            throw new IllegalArgumentException("Season profiles are not correctly configured.");
                                        }
                                    } else {
                                        dayTypeIds[3] = rule.dayTypeId();
                                    }
                                }
                                break;
                                case 5: {
                                    if (dayTypeIds[4] != null) {
                                        if (dayTypeIds[4] != rule.dayTypeId()) {
                                            throw new IllegalArgumentException("Season profiles are not correctly configured.");
                                        }
                                    } else {
                                        dayTypeIds[4] = rule.dayTypeId();
                                    }
                                }
                                break;
                                case 6: {
                                    if (dayTypeIds[5] != null) {
                                        if (dayTypeIds[5] != rule.dayTypeId()) {
                                            throw new IllegalArgumentException("Season profiles are not correctly configured.");
                                        }
                                    } else {
                                        dayTypeIds[5] = rule.dayTypeId();
                                    }
                                }
                                break;
                                case 7: {
                                    if (dayTypeIds[6] != null) {
                                        if (dayTypeIds[6] != rule.dayTypeId()) {
                                            throw new IllegalArgumentException("Season profiles are not correctly configured.");
                                        }
                                    } else {
                                        dayTypeIds[6] = rule.dayTypeId();
                                    }
                                }
                                break;
                                case -1: {
                                    if (any != null) {
                                        if (any != rule.dayTypeId()) {
                                            throw new IllegalArgumentException("Season profiles are not correctly configured.");
                                        }
                                    } else {
                                        any = rule.dayTypeId();
                                    }
                                }
                                break;
                                default:
                                    throw new IllegalArgumentException("Undefined daytype code received.");
                            }
                        }
                    }

                    wp.setWeekProfileName(getOctetStringFromInt(weekProfileName));
                    for (int i = 0; i < dayTypeIds.length; i++) {
                        if (dayTypeIds[i] != null) {
                            wp.addWeekDay(dayTypeIds[i], i);
                        } else if (any != null) {
                            wp.addWeekDay(any, i);
                        } else {
                            throw new IllegalArgumentException("Not all dayId's are correctly filled in.");
                        }
                    }
                    weekArray.addDataType(wp);

                }
            }
        }

        seasonArray = sort(seasonArray);

        for (Extractor.CalendarDayType codeDayType : extractor.dayTypes(calendar)) {
            DayProfiles dayProfile = new DayProfiles();
            List<Extractor.CalendarDayTypeSlice> slices = codeDayType.slices();
            Array daySchedules = new Array();
            for (Extractor.CalendarDayTypeSlice slice : slices) {
                DayProfileActions dayProfileActions = new DayProfileActions();
                LocalTime start = slice.start();
                int hour = start.getHour() / 10000;
                int min = start.getMinute();
                int sec = start.getSecond();
                OctetString tstampOs = OctetString.fromByteArray(new byte[]{(byte) hour, (byte) min, (byte) sec, 0});
                Unsigned16 selector = new Unsigned16(Integer.parseInt(slice.tariffCode()));
                dayProfileActions.setStartTime(tstampOs);
                if (this.meterConfig == null) {
                    byte[] ln = ObisCode.fromString("0.0.10.0.100.255").getLN();
                    dayProfileActions.setScriptLogicalName(OctetString.fromByteArray(ln, ln.length));
                } else {
                    dayProfileActions.setScriptLogicalName(OctetString.fromByteArray(this.meterConfig.getTariffScriptTable().getLNArray()));
                }
                dayProfileActions.setScriptSelector(selector);
                daySchedules.addDataType(dayProfileActions);
            }
            dayProfile.setDayId(new Unsigned8(Integer.parseInt(codeDayType.id())));
            dayProfile.setDayProfileActions(daySchedules);
            dayArray.addDataType(dayProfile);
        }

        checkForSingleSeasonStartDate();
    }

    protected Array sort(Array seasonArray) {
        //No need for sorting here, subclasses can override though.
        return seasonArray;
    }

    protected OctetString getOctetStringFromInt(int weekProfileName) {
        return OctetString.fromString(Integer.toString(weekProfileName));
    }

    protected String getSeasonProfileName(Map.Entry<OctetString, String> entry) {
        return entry.getValue();
    }

    /**
     * Checks if a given seasonProfile already exists
     *
     * @param seasonProfileNameId - the id of the 'to-check' seasonProfile
     * @param seasonArray         - the complete seasonProfile list where you need to check in
     * @return true if it exists, false otherwise
     */
    private boolean seasonArrayExists(String seasonProfileNameId, Array seasonArray) {
        for (int i = 0; i < seasonArray.nrOfDataTypes(); i++) {
            SeasonProfiles sp = (SeasonProfiles) seasonArray.getDataType(i);
            if (getSeasonIdFromSeasonProfile(sp) == seasonProfileNameId) {
                return true;
            }
        }
        return false;
    }

    protected String getSeasonIdFromSeasonProfile(SeasonProfiles sp) {
        return sp.getSeasonId();
    }

    /**
     * Checks if a given weekProfile already exists
     *
     * @param weekProfileName - the id of the 'to-check' weekProfile
     * @param weekArray       - the complete weekProfile list where you need to check in
     * @return true if it exists, false otherwise
     */
    private boolean weekArrayExists(int weekProfileName, Array weekArray) {
        for (int i = 0; i < weekArray.nrOfDataTypes(); i++) {
            WeekProfiles wp = (WeekProfiles) weekArray.getDataType(i);
            if (ProtocolTools.getIntFromBytes(wp.getWeekProfileName().getOctetStr()) == weekProfileName) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the current seasonArray
     */
    public Array getSeasonProfile() {
        return this.seasonArray;
    }

    /**
     * If only one season is defined, then you must at least enter 1 value for the startDate.
     */
    private void checkForSingleSeasonStartDate() {
        if (this.seasonArray.nrOfDataTypes() == 1) {
            SeasonProfiles sp = (SeasonProfiles) this.seasonArray.getDataType(0);
            byte[] startTime = sp.getSeasonStart().getOctetStr();
            startTime[2] = 1;    // set the startTime to the first of the month
            startTime[3] = 1;    // set the startTime to the first of the month
            sp.setSeasonStart(OctetString.fromByteArray(startTime));
            //TODO check if you need to add it again.
        }
    }

    /**
     * @return the current weekArray
     */
    public Array getWeekProfile() {
        return this.weekArray;
    }

    /**
     * @return the current dayArray
     */
    public Array getDayProfile() {
        return this.dayArray;
    }
}
