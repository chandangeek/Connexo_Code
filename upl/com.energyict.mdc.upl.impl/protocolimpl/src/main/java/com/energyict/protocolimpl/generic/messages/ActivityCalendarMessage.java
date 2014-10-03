package com.energyict.protocolimpl.generic.messages;

import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.attributeobjects.*;
import com.energyict.mdw.core.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.*;

public class ActivityCalendarMessage {

    private Code ct;
    private Array dayArray;
    private Array seasonArray;
    private Array weekArray;
    private DLMSMeterConfig meterConfig;
    protected Map<Integer, Integer> seasonIds = new HashMap<Integer, Integer>();  //Map the DB id's of the seasons to a proper 0-based index that can be used in the AXDR array
    protected Map<Integer, Integer> dayTypeIds = new HashMap<Integer, Integer>();  //Map the DB id's of the day types to a proper 0-based index that can be used in the AXDR array

    public ActivityCalendarMessage(Code ct, DLMSMeterConfig meterConfig) {
        this.ct = ct;
        this.meterConfig = meterConfig;
        this.dayArray = new Array();
        this.seasonArray = new Array();
        this.weekArray = new Array();
        this.seasonIds = new HashMap<Integer, Integer>();
        this.dayTypeIds = new HashMap<Integer, Integer>();
    }

    /**
     * Parsing of the codeTable to season-, week- and dayProfiles
     *
     * @throws IOException when CodeTable is not correctly configured
     */
    public void parse() throws IOException {
        List calendars = ct.getCalendars();
        HashMap<OctetString, Integer> seasonsProfile = new HashMap<OctetString, Integer>();

        Iterator itr = calendars.iterator();
        int index = 0;
        while (itr.hasNext()) {
            CodeCalendar cc = (CodeCalendar) itr.next();
            int seasonId = cc.getSeason();
            if (seasonId != 0) {
                OctetString os = OctetString.fromByteArray(new byte[]{(byte) ((cc.getYear() == -1) ? 0xff : ((cc.getYear() >> 8) & 0xFF)), (byte) ((cc.getYear() == -1) ? 0xff : (cc.getYear()) & 0xFF),
                        (byte) ((cc.getMonth() == -1) ? 0xFF : cc.getMonth()), (byte) ((cc.getDay() == -1) ? 0xFF : cc.getDay()), (byte) 0xFF, 0, 0, 0, 0, (byte) 0x80, 0, 0});
                seasonsProfile.put(os, seasonId);
                if (!seasonIds.containsKey(seasonId)) {
                    seasonIds.put(seasonId, index);
                    index++;
                }
            }
        }

        //Create day type IDs (incremental 0-based)
        for (int dayTypeIndex = 0; dayTypeIndex < ct.getDayTypes().size(); dayTypeIndex++) {
            CodeDayType dayType = ct.getDayTypes().get(dayTypeIndex);
            if (!dayTypeIds.containsKey(dayType.getId())) {
                dayTypeIds.put(dayType.getId(), dayTypeIndex);
            }
        }

        int weekIndex = 0;
        for (Map.Entry<OctetString, Integer> entry : seasonsProfile.entrySet()) {

            int seasonProfileNameId = getSeasonProfileName(entry);
            if (!seasonArrayExists(seasonProfileNameId, seasonArray)) {

                int weekProfileName = weekIndex;
                weekIndex++;
                SeasonProfiles seasonProfiles = new SeasonProfiles();
                seasonProfiles.setSeasonProfileName(getOctetStringFromInt(seasonProfileNameId));    // the seasonProfileName is the DB id of the season
                seasonProfiles.setSeasonStart(entry.getKey());
                seasonProfiles.setWeekName(getOctetStringFromInt(weekProfileName));
                seasonArray.addDataType(seasonProfiles);
                if (!weekArrayExists(weekProfileName, weekArray)) {
                    WeekProfiles wp = new WeekProfiles();
                    Iterator sIt = calendars.iterator();
                    CodeDayType dayTypes[] = {null, null, null, null, null, null, null};
                    CodeDayType any = null;
                    while (sIt.hasNext()) {
                        CodeCalendar codeCal = (CodeCalendar) sIt.next();
                        if (codeCal.getSeason() == entry.getValue()) {
                            switch (codeCal.getDayOfWeek()) {
                                case 1: {
                                    if (dayTypes[0] != null) {
                                        if (dayTypes[0] != codeCal.getDayType()) {
                                            throw new IOException("Season profiles are not correctly configured.");
                                        }
                                    } else {
                                        dayTypes[0] = codeCal.getDayType();
                                    }
                                }
                                break;
                                case 2: {
                                    if (dayTypes[1] != null) {
                                        if (dayTypes[1] != codeCal.getDayType()) {
                                            throw new IOException("Season profiles are not correctly configured.");
                                        }
                                    } else {
                                        dayTypes[1] = codeCal.getDayType();
                                    }
                                }
                                break;
                                case 3: {
                                    if (dayTypes[2] != null) {
                                        if (dayTypes[2] != codeCal.getDayType()) {
                                            throw new IOException("Season profiles are not correctly configured.");
                                        }
                                    } else {
                                        dayTypes[2] = codeCal.getDayType();
                                    }
                                }
                                break;
                                case 4: {
                                    if (dayTypes[3] != null) {
                                        if (dayTypes[3] != codeCal.getDayType()) {
                                            throw new IOException("Season profiles are not correctly configured.");
                                        }
                                    } else {
                                        dayTypes[3] = codeCal.getDayType();
                                    }
                                }
                                break;
                                case 5: {
                                    if (dayTypes[4] != null) {
                                        if (dayTypes[4] != codeCal.getDayType()) {
                                            throw new IOException("Season profiles are not correctly configured.");
                                        }
                                    } else {
                                        dayTypes[4] = codeCal.getDayType();
                                    }
                                }
                                break;
                                case 6: {
                                    if (dayTypes[5] != null) {
                                        if (dayTypes[5] != codeCal.getDayType()) {
                                            throw new IOException("Season profiles are not correctly configured.");
                                        }
                                    } else {
                                        dayTypes[5] = codeCal.getDayType();
                                    }
                                }
                                break;
                                case 7: {
                                    if (dayTypes[6] != null) {
                                        if (dayTypes[6] != codeCal.getDayType()) {
                                            throw new IOException("Season profiles are not correctly configured.");
                                        }
                                    } else {
                                        dayTypes[6] = codeCal.getDayType();
                                    }
                                }
                                break;
                                case -1: {
                                    if (any != null) {
                                        if (any != codeCal.getDayType()) {
                                            throw new IOException("Season profiles are not correctly configured.");
                                        }
                                    } else {
                                        any = codeCal.getDayType();
                                    }
                                }
                                break;
                                default:
                                    throw new IOException("Undefined daytype code received.");
                            }
                        }
                    }

                    wp.setWeekProfileName(getOctetStringFromInt(weekProfileName));
                    for (int i = 0; i < dayTypes.length; i++) {
                        if (dayTypes[i] != null) {
                            wp.addWeekDay(getDayTypeName(dayTypes[i]), i);
                        } else if (any != null) {
                            wp.addWeekDay(getDayTypeName(any), i);
                        } else {
                            throw new IOException("Not all dayId's are correctly filled in.");
                        }
                    }
                    weekArray.addDataType(wp);

                }
            }
        }

        seasonArray = sort(seasonArray);

        for (CodeDayType codeDayType : ct.getDayTypesOfCalendar()) {
            DayProfiles dayProfile = new DayProfiles();
            List<CodeDayTypeDef> definitions = codeDayType.getDefinitions();
            Array daySchedules = new Array();
            for (CodeDayTypeDef definition : definitions) {
                DayProfileActions dayProfileActions = new DayProfileActions();
                int tStamp = definition.getTstampFrom();
                int hour = tStamp / 10000;
                int min = (tStamp - hour * 10000) / 100;
                int sec = tStamp - (hour * 10000) - (min * 100);
                OctetString tstampOs = OctetString.fromByteArray(new byte[]{(byte) hour, (byte) min, (byte) sec, 0});
                Unsigned16 selector = new Unsigned16(definition.getCodeValue());
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
            dayProfile.setDayId(new Unsigned8(getDayTypeName(codeDayType)));
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

    protected Integer getSeasonProfileName(Map.Entry<OctetString, Integer> entry) {
        return entry.getValue();
    }

    protected int getDayTypeName(CodeDayType cdt) {
        return cdt.getId();
    }

    /**
     * Checks if a given seasonProfile already exists
     *
     * @param seasonProfileNameId - the id of the 'to-check' seasonProfile
     * @param seasonArray         - the complete seasonProfile list where you need to check in
     * @return true if it exists, false otherwise
     */
    private boolean seasonArrayExists(int seasonProfileNameId, Array seasonArray) {
        for (int i = 0; i < seasonArray.nrOfDataTypes(); i++) {
            SeasonProfiles sp = (SeasonProfiles) seasonArray.getDataType(i);
            if (getSeasonIdFromSeasonProfile(sp) == seasonProfileNameId) {
                return true;
            }
        }
        return false;
    }

    protected int getSeasonIdFromSeasonProfile(SeasonProfiles sp) {
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
