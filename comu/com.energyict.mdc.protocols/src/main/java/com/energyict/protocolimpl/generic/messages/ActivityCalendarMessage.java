/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.generic.messages;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.EventOccurrence;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.ProtocolException;

import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.attributeobjects.DayProfileActions;
import com.energyict.dlms.cosem.attributeobjects.DayProfiles;
import com.energyict.dlms.cosem.attributeobjects.SeasonProfiles;
import com.energyict.dlms.cosem.attributeobjects.WeekProfiles;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ActivityCalendarMessage {

    private Calendar calendar;
    private Array dayArray;
    private Array seasonArray;
    private Array weekArray;
    private DLMSMeterConfig meterConfig;
    public Map<Long, Integer> periodIds = new HashMap<>();  //Map the DB id's of the periods to a proper 0-based index that can be used in the AXDR array
    protected Map<Long, Integer> dayTypeIds = new HashMap<>();  //Map the DB id's of the day types to a proper 0-based index that can be used in the AXDR array

    public ActivityCalendarMessage(Calendar calendar, DLMSMeterConfig meterConfig) {
        this.calendar = calendar;
        this.meterConfig = meterConfig;
        this.dayArray = new Array();
        this.seasonArray = new Array();
        this.weekArray = new Array();
        this.periodIds = new HashMap<>();
        this.dayTypeIds = new HashMap<>();
    }

    /**
     * Parsing of the codeTable to season-, week- and dayProfiles
     *
     * @throws java.io.IOException when Calendar is not correctly configured
     */
    public void parse() throws IOException {
        this.initializeDayTypeIds();
        this.initializeSeasonAndWeekArray();
        try {
            this.calendar.getDayTypes().stream().forEach(this::addActionsForDayType);
        } catch (UnderlyingProtocolException e) {
            throw e.getCause();
        }
        checkForSingleSeasonStartDate();
    }

    private void addActionsForDayType(DayType dayType) {
        try {
            DayProfiles dayProfile = new DayProfiles();
            Array daySchedules = new Array();
            boolean first = true;
            DayProfileActions fromMidnightToFirst = null;   // We may not need it if the first occurrence starts at midnight
            EventOccurrence lastEventOccurrence = null;
            for (EventOccurrence eventOccurrence : dayType.getEventOccurrences()) {
                if (first && !eventOccurrence.getFrom().equals(LocalTime.MIDNIGHT)) {
                    /* First entry does not start at midnight,
                     * add an additional one that will get the same code
                     * as the last event occurrence. */
                    first = false;
                    fromMidnightToFirst = this.newAction(LocalTime.MIDNIGHT);
                    daySchedules.addDataType(fromMidnightToFirst);
                }
                DayProfileActions dayProfileActions = this.newAction(eventOccurrence);
                daySchedules.addDataType(dayProfileActions);
                lastEventOccurrence = eventOccurrence;
            }
            if (fromMidnightToFirst != null && lastEventOccurrence != null) {
                /* If a slot was created from midnight to the first event occurrence
                 * then we use the same code as the last event occurrence for that slot. */
                fromMidnightToFirst.setScriptSelector(this.selectorFor(lastEventOccurrence));
            }
            dayProfile.setDayId(new Unsigned8(getDayTypeName(dayType)));
            dayProfile.setDayProfileActions(daySchedules);
            this.dayArray.addDataType(dayProfile);
        } catch (ProtocolException e) {
            throw new UnderlyingProtocolException(e);
        }
    }

    private DayProfileActions newAction(LocalTime start) throws ProtocolException {
        OctetString startTime = OctetString.fromByteArray(new byte[]{(byte) start.getHour(), (byte) start.getMinute(), (byte) start.getSecond(), 0});
        DayProfileActions dayProfileActions = new DayProfileActions();
        dayProfileActions.setStartTime(startTime);
        if (this.meterConfig == null) {
            byte[] ln = ObisCode.fromString("0.0.10.0.100.255").getLN();
            dayProfileActions.setScriptLogicalName(OctetString.fromByteArray(ln, ln.length));
        } else {
            dayProfileActions.setScriptLogicalName(OctetString.fromByteArray(this.meterConfig.getTariffScriptTable().getLNArray()));
        }
        return dayProfileActions;
    }

    private DayProfileActions newAction(EventOccurrence eventOccurrence) throws ProtocolException {
        DayProfileActions dayProfileActions = this.newAction(eventOccurrence.getFrom());
        dayProfileActions.setScriptSelector(this.selectorFor(eventOccurrence));
        return dayProfileActions;
    }

    private Unsigned16 selectorFor(EventOccurrence eventOccurrence) {
        return new Unsigned16((int) eventOccurrence.getEvent().getCode());
    }

    private void initializeSeasonAndWeekArray() {
        Map<OctetString, Long> seasonsProfile = this.initializeSeasonsProfile();
        int weekIndex = 0;
        for (Map.Entry<OctetString, Long> entry : seasonsProfile.entrySet()) {
            long seasonProfileNameId = getSeasonProfileName(entry);
            if (!seasonArrayExists(seasonProfileNameId, seasonArray)) {
                int weekProfileName = weekIndex;
                weekIndex++;
                SeasonProfiles seasonProfiles = new SeasonProfiles();
                seasonProfiles.setSeasonProfileName(getOctetStringFromLong(seasonProfileNameId));    // the seasonProfileName is the DB id of the season
                seasonProfiles.setSeasonStart(entry.getKey());
                seasonProfiles.setWeekName(getOctetStringFromLong(weekProfileName));
                seasonArray.addDataType(seasonProfiles);
                if (!weekArrayExists(weekProfileName, weekArray)) {
                    DayType dayTypes[] = {null, null, null, null, null, null, null};
                    this.calendar.getPeriods().forEach(period -> {
                        if (period.getId() == entry.getValue()) {
                            dayTypes[0] = period.getDayType(DayOfWeek.MONDAY);
                            dayTypes[1] = period.getDayType(DayOfWeek.TUESDAY);
                            dayTypes[2] = period.getDayType(DayOfWeek.WEDNESDAY);
                            dayTypes[3] = period.getDayType(DayOfWeek.THURSDAY);
                            dayTypes[4] = period.getDayType(DayOfWeek.FRIDAY);
                            dayTypes[5] = period.getDayType(DayOfWeek.SATURDAY);
                            dayTypes[6] = period.getDayType(DayOfWeek.SUNDAY);
                        }
                    });
                    WeekProfiles wp = new WeekProfiles();
                    wp.setWeekProfileName(getOctetStringFromLong(weekProfileName));
                    for (int i = 0; i < dayTypes.length; i++) {
                        wp.addWeekDay(getDayTypeName(dayTypes[i]), i);
                    }
                    weekArray.addDataType(wp);
                }
            }
        }
        seasonArray = sort(seasonArray);
    }

    private Map<OctetString, Long> initializeSeasonsProfile() {
        ArrayIndexGenerator periodIndex = ArrayIndexGenerator.zeroBased();
        Map<OctetString, Long> map = new HashMap<>();
        this.calendar.getPeriods().forEach(period -> {
            // All usages of day types in a period have any year, any month and any day
            OctetString os =
                    OctetString.fromByteArray(
                            new byte[]{
                                    (byte) 0xff,
                                    (byte) 0xff,
                                    (byte) 0xFF,
                                    (byte) 0xFF,
                                    (byte) 0xFF,
                                    0,
                                    0,
                                    0,
                                    0,
                                    (byte) 0x80,
                                    0,
                                    0});
            map.put(os, period.getId());
            this.periodIds.computeIfAbsent(period.getId(), id -> periodIndex.next());
        });
        return map;
    }

    private void initializeDayTypeIds() {
        ArrayIndexGenerator dayTypeIndex = ArrayIndexGenerator.zeroBased();
        this.dayTypeIds =
            this.calendar
                .getDayTypes()
                .stream()
                .collect(Collectors.toMap(
                    DayType::getId,
                    dayType1 -> dayTypeIndex.next()));
    }

    protected Array sort(Array seasonArray) {
        //No need for sorting here, subclasses can override though.
        return seasonArray;
    }

    protected OctetString getOctetStringFromLong(long weekProfileName) {
        return OctetString.fromString(Long.toString(weekProfileName));
    }

    protected Long getSeasonProfileName(Map.Entry<OctetString, Long> entry) {
        return entry.getValue();
    }

    protected int getDayTypeName(DayType dayType) {
        return (int) dayType.getId();
    }

    /**
     * Checks if a given seasonProfile already exists
     *
     * @param seasonProfileNameId - the id of the 'to-check' seasonProfile
     * @param seasonArray         - the complete seasonProfile list where you need to check in
     * @return true if it exists, false otherwise
     */
    private boolean seasonArrayExists(long seasonProfileNameId, Array seasonArray) {
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

    private static class UnderlyingProtocolException extends RuntimeException {
        private final ProtocolException cause;

        private UnderlyingProtocolException(ProtocolException cause) {
            this.cause = cause;
        }

        @Override
        public ProtocolException getCause() {
            return cause;
        }
    }

}