/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.profile;

import com.energyict.protocolimplv2.elster.ctr.MTU155.RequestFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.PeriodTrace_C;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.ReferenceDate;
import com.energyict.protocolimplv2.elster.ctr.MTU155.util.DstSettings;

import java.util.Calendar;

import static com.energyict.protocolimpl.utils.ProtocolTools.isInDST;

/**
 * @author sva
 * @since 18/02/13 - 17:24
 */
public class StartOfGasDayParser {

    RequestFactory requestFactory;

    private int gasDayStartEndTime = -1;
    private int dayLightSavingEnabledValue = -1;
    private int ek155GasDayStartEndTime = -1;
    private int ek155TimeInUCTValue = -1;
    private boolean isEK155;

    /**
     * Constructor to be used for regular GPRS
     *
     * @param requestFactory
     * @throws CTRException
     */
    public StartOfGasDayParser(RequestFactory requestFactory) throws CTRException {
        this.requestFactory = requestFactory;

        DstSettings dstSettings = new DstSettings(requestFactory);
        gasDayStartEndTime = dstSettings.getGasDayStartEndTime();
        dayLightSavingEnabledValue = dstSettings.getDayLightSavingEnabledValue();

        if (isEK155Protocol()) {
            ek155GasDayStartEndTime = dstSettings.getEK155GasDayStartEndTime();
            ek155TimeInUCTValue = dstSettings.getEK155TimeInUCTValue();
        }
    }

    /**
     * Constructor to be used for SMS & unit testing
     *
     * @param isEK155
     * @param gasDayStartEndTime
     * @param dayLightSavingEnabled
     * @param ek155GasDayStartEndTime
     * @param ek155TimeInUCT
     */
    public StartOfGasDayParser(Boolean isEK155, int gasDayStartEndTime, boolean dayLightSavingEnabled, int ek155GasDayStartEndTime, boolean ek155TimeInUCT) {
        this.requestFactory = null;
        this.isEK155 = isEK155;
        this.gasDayStartEndTime = gasDayStartEndTime;
        this.dayLightSavingEnabledValue = dayLightSavingEnabled ? 1 : 0;
        this.ek155GasDayStartEndTime = ek155GasDayStartEndTime;
        this.ek155TimeInUCTValue = ek155TimeInUCT ? 1 : 0;
    }

    public ReferenceDate calcRefDate(Calendar from, PeriodTrace_C period) {
        ReferenceDate date = new ReferenceDate();
        if (period.isHourly()) {
            date.parse(getStartOfGasDay(from));
        } else if (period.isHourlyFistPart() || period.isHourlySecondPart()) {
            date.parse(getStartOfGasDay(from));
        } else if (period.isDaily()) {
            Calendar startDay = getStartOfGasDay(from);
            startDay.add(Calendar.DAY_OF_YEAR, period.getTraceCIntervalCount() - 1);
            date.parse(startDay);
        } else if (period.isMonthly()) {
            date.parse(getStartOfGasDay(from));
        }
        return date;
    }

    public Calendar getStartOfGasDay(Calendar from) {
        Calendar startOfGasDay = (Calendar) from.clone();
        if (from.get(Calendar.HOUR_OF_DAY) < getStartOfGasDayHour(from)) {
            startOfGasDay.add(Calendar.DAY_OF_YEAR, -1);
        }
        startOfGasDay.set(Calendar.HOUR_OF_DAY, getStartOfGasDayHour(startOfGasDay));
        startOfGasDay.set(Calendar.MINUTE, 0);
        startOfGasDay.set(Calendar.SECOND, 0);
        startOfGasDay.set(Calendar.MILLISECOND, 0);
        return startOfGasDay;
    }

    private int getStartOfGasDayHour(Calendar from) {
        if (isEK155Protocol()) {
            final int ek155GasDayStartEndTime = getEk155GasDayStartEndTime();
            final boolean ek155ExpressedInUTC = isEk155ExpressedInUTC();
            final boolean dstEnabled = isDstEnabled();

            Calendar calendar = (Calendar) from.clone();
            calendar.set(Calendar.HOUR_OF_DAY, ek155GasDayStartEndTime);
            if (dstEnabled && ek155ExpressedInUTC && isInDST(calendar)) {
                return ek155GasDayStartEndTime + 1; // hourVal is in UTC - should be converted to device timezone
            } else {
                return ek155GasDayStartEndTime;     // hourVal is following device timezone
            }
        } else {
            return getGasDayStartEndTime();             // OFG val always to be interpreted as being in device timezone
        }
    }

    public int getStartOfGasDayHour(Calendar from, int traceCEndOfDayTime) {
        if (isEK155Protocol()) {
            final boolean ek155ExpressedInUTC = isEk155ExpressedInUTC();
            final boolean dstEnabled = isDstEnabled();

            Calendar calendar = (Calendar) from.clone();
            calendar.set(Calendar.HOUR_OF_DAY, traceCEndOfDayTime);
            if (dstEnabled && ek155ExpressedInUTC && isInDST(calendar)) {
                return traceCEndOfDayTime + 1;  // hourVal is in UTC - should be converted to device timezone
            } else {
                return traceCEndOfDayTime;      // hourVal is following device timezone
            }
        } else {
            return traceCEndOfDayTime;          // OFG val always to be interpreted as being in device timezone
        }
    }

    public int getGasDayStartEndTime() {
        return gasDayStartEndTime;
    }

    public boolean isDstEnabled() {
        return dayLightSavingEnabledValue == 1;
    }

    public int getEk155GasDayStartEndTime() {
        return ek155GasDayStartEndTime;
    }

    public boolean isEk155ExpressedInUTC() {
        return ek155TimeInUCTValue == 1;
    }

    private Boolean isEK155Protocol() {
        if (getRequestFactory() != null) {
            return getRequestFactory().isEK155Protocol();
        } else {
            return isEK155;
        }
    }

    private RequestFactory getRequestFactory() {
        return requestFactory;
    }
}
