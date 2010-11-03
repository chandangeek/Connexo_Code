package com.energyict.genericprotocolimpl.elster.ctr.profile;

import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.elster.ctr.GprsRequestFactory;
import com.energyict.genericprotocolimpl.elster.ctr.MTU155Properties;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.Qualifier;
import com.energyict.genericprotocolimpl.elster.ctr.structure.Trace_CQueryResponseStructure;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.PeriodTrace;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.ReferenceDate;
import com.energyict.genericprotocolimpl.elster.ctr.util.CTRObjectInfo;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Rtu;
import com.energyict.protocol.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 18-okt-2010
 * Time: 16:14:08
 */
public class ProfileChannel {

    private final GprsRequestFactory requestFactory;
    private final Channel meterChannel;
    private Date meterClock;
    private ReferenceDate lastReferenceDate;
    private Trace_CQueryResponseStructure response;
    private TimeZone timeZone;

    public ProfileChannel(GprsRequestFactory requestFactory, Channel meterChannel) {
        this(requestFactory, meterChannel, null);
    }

    public ProfileChannel(GprsRequestFactory requestFactory, Channel meterChannel, TimeZone timeZone) {
        this.requestFactory = requestFactory;
        this.meterChannel = meterChannel;
        if (timeZone == null) {
            this.meterClock = new Date();
        } else {
            this.timeZone = timeZone;
        }
    }

    private Date getTimeFromTrace_C(CTRAbstractValue<BigDecimal>[] values) {

        Calendar cal = Calendar.getInstance(timeZone);

        int ptr = 0;
        int year = values[ptr++].getValue().intValue() + 2000;
        int month = values[ptr++].getValue().intValue() - 1;
        int day = values[ptr++].getValue().intValue();
        int hour = values[ptr++].getValue().intValue();
        int min = values[ptr++].getValue().intValue();

        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return fixDate(cal.getTime());
    }

    //Checks if min > 60 or hours > 24 (indicates a time shift is in progress)
    private Date fixDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minutes = cal.get(Calendar.MINUTE);

        if (hour > 23) {
            hour -= 30;
            cal.set(Calendar.HOUR_OF_DAY, hour);
        }
        if (minutes > 59) {
            minutes -= 60;
            cal.set(Calendar.MINUTE, minutes);
        }

        return cal.getTime();
    }


    /**
     * @return
     */
    private GprsRequestFactory getRequestFactory() {
        return requestFactory;
    }

    /**
     * @return
     */
    private MTU155Properties getProperties() {
        return getRequestFactory().getProperties();
    }

    /**
     * @return
     */
    private Logger getLogger() {
        return getRequestFactory().getLogger();
    }

    /**
     * @return
     */
    private TimeZone getDeviceTimeZone() {
        return getRtu().getDeviceTimeZone();
    }

    /**
     * @return
     */
    private Rtu getRtu() {
        return getMeterChannel().getRtu();
    }

    /**
     * @return
     */
    public Channel getMeterChannel() {
        return meterChannel;
    }

    /**
     * @return
     */
    private String getChannelObjectId() {
        return getProperties().getChannelConfig().getChannelObjectId(getChannelIndex() - 1);
    }

    /**
     * @return
     */
    private int getChannelIndex() {
        return getMeterChannel().getLoadProfileIndex();
    }

    public ReferenceDate getLastReferenceDate() {
        return lastReferenceDate;
    }

    public Date getMeterClock() {
        if (meterClock == null) {
            this.meterClock = getTimeFromTrace_C(response.getDateAndhourS());
        }
        return meterClock;
    }

    /**
     * @return
     * @throws CTRException
     */
    public ProfileData getProfileData() throws CTRException {
        if (getChannelObjectId() == null) {
            getLogger().warning("No channel config found for channel with loadProfileIndex [" + getChannelIndex() + "]");
            return new ProfileData();
        }

        ProfileData pd = new ProfileData();
        pd.setChannelInfos(getChannelInfos());
        pd.setIntervalDatas(getIntervalData());
        return pd;
    }

    /**
     * @return
     */
    private List<ChannelInfo> getChannelInfos() {
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
        String symbol = CTRObjectInfo.getSymbol(getChannelObjectId()) + " [" + getChannelObjectId() + "]";
        Unit unit = CTRObjectInfo.getUnit(getChannelObjectId());
        ChannelInfo info = new ChannelInfo(0, getChannelIndex() - 1, symbol, unit);
        channelInfos.add(info);
        return channelInfos;
    }

    /**
     * @throws CTRException
     */
    private List<IntervalData> getIntervalData() throws CTRException {
        List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
        Calendar fromCalendar = getFromCalendar();
        Calendar toCalendar = Calendar.getInstance(getDeviceTimeZone());
        lastReferenceDate = null;
        while (fromCalendar.before(toCalendar)) {
            try {
                intervalDatas.addAll(getIntervalDataBlock(getChannelObjectId(), fromCalendar));
                fromCalendar.setTime(getNewestIntervalDate(intervalDatas));
            } catch (CTREndOfProfileException e) {
                break;
            }
        }
        return intervalDatas;
    }

    /**
     * @param intervalDatas
     * @return
     */
    private Date getNewestIntervalDate(List<IntervalData> intervalDatas) {
        Date newest = new Date(0);
        for (IntervalData intervalData : intervalDatas) {
            if (intervalData.getEndTime().after(newest)) {
                newest = intervalData.getEndTime();
            }
        }
        return newest;
    }

    /**
     * @param channelId
     * @param fromCalendar
     * @return
     * @throws CTRException
     */
    private List<IntervalData> getIntervalDataBlock(String channelId, Calendar fromCalendar) throws CTRException {
        ReferenceDate referenceDate = new ReferenceDate().parse(fromCalendar);
        if (getLastReferenceDate() != null) {
            if (getLastReferenceDate().getCalendar(getDeviceTimeZone()).getTimeInMillis() >= referenceDate.getCalendar(getDeviceTimeZone()).getTimeInMillis()) {
                throw new CTREndOfProfileException("Profile loop detected. Requested same date as previous request!");
            }
        }
        lastReferenceDate = new ReferenceDate().parse(referenceDate.getBytes(), 0);
        CTRObjectID objectID = new CTRObjectID(channelId);
        PeriodTrace period = new PeriodTrace(2);
        response = getRequestFactory().queryTrace_C(objectID, period, referenceDate);
        return getIntervalDatasFromResponse(response);
    }

    /**
     * @param response
     * @return
     */
    private List<IntervalData> getIntervalDatasFromResponse(Trace_CQueryResponseStructure response) {
        List<IntervalData> intervals = new ArrayList<IntervalData>();
        Calendar startDate = response.getDate().getCalendar(getDeviceTimeZone());
        int startOfDay = response.getEndOfDayTime().getIntValue();
        startDate.add(Calendar.HOUR, startOfDay);
        int interval = getMeterChannel().getIntervalInSeconds();

        for (int i = 0; i < response.getTraceData().size(); i++) {
            if (i < response.getPeriod().getTraceCIntervalCount()) {
                AbstractCTRObject object = response.getTraceData().get(i);
                startDate.add(Calendar.SECOND, interval);
                Date endDate = new Date(startDate.getTimeInMillis());
                List<IntervalValue> intervalValues = new ArrayList<IntervalValue>();
                Qualifier qlf = object.getQlf();
                Object objectValue = object.getValue(0).getValue();
                if (objectValue instanceof Number) {
                    Number number = (Number) objectValue;
                    if (number instanceof BigDecimal) {
                        BigDecimal decimal = (BigDecimal) number;
                        decimal = decimal.movePointRight(qlf.getKmoltFactor());
                        intervalValues.add(new IntervalValue(decimal, 0, 0));
                    } else {
                        intervalValues.add(new IntervalValue(number, 0, 0));
                    }
                }
                if (endDate.before(getMeterClock())) {
                    intervals.add(new IntervalData(endDate, getIntervalStateBits(qlf), qlf.getQlf(), 0, intervalValues));
                } else {
                    break;
                }
            }
        }
        return intervals;
    }

    private int getIntervalStateBits(Qualifier qualifier) {
        if (qualifier.isInvalidMeasurement()) {
            return IntervalStateBits.CORRUPTED;
        } else if (qualifier.isSubjectToMaintenance()) {
            return IntervalStateBits.OTHER;
        } else if (qualifier.isReservedVal()) {
            return IntervalStateBits.OTHER;
        } else {
            return IntervalStateBits.OK;
        }
    }

    /**
     * @return
     */
    private Calendar getFromCalendar() {
        Date lastReading = getMeterChannel().getLastReading();
        if (lastReading == null) {
            lastReading = com.energyict.genericprotocolimpl.common.ParseUtils.getClearLastMonthDate(getRtu());
        }
        Calendar cal = ProtocolUtils.getCleanCalendar(getDeviceTimeZone());
        cal.setTime(lastReading);
        return cal;
    }

    private class CTREndOfProfileException extends CTRException {
        public CTREndOfProfileException(String message) {
            super(message);
        }
    }
}
