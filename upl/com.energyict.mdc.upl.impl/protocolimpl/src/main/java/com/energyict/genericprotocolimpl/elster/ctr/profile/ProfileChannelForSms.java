package com.energyict.genericprotocolimpl.elster.ctr.profile;

import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.elster.ctr.MTU155Properties;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.Qualifier;
import com.energyict.genericprotocolimpl.elster.ctr.structure.Trace_CQueryResponseStructure;
import com.energyict.genericprotocolimpl.elster.ctr.util.CTRObjectInfo;
import com.energyict.genericprotocolimpl.webrtuz3.MeterAmrLogging;
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
public class ProfileChannelForSms {

    private final Channel meterChannel;
    private MTU155Properties properties;
    private final Date meterClock;
    private Logger logger;
    private TimeZone timeZone;
    private Trace_CQueryResponseStructure response;
    private MeterAmrLogging meterAmrLogging;

    public ProfileChannelForSms(Logger logger, MTU155Properties properties, Channel meterChannel, Trace_CQueryResponseStructure response, TimeZone timeZone, MeterAmrLogging meterAmrLogging) {
        this.properties = properties;
        this.meterChannel = meterChannel;
        this.logger = logger;
        this.response = response;
        this.timeZone = timeZone;
        this.meterClock = getTimeFromTrace_C(response.getDateAndhourS());
        this.meterAmrLogging = meterAmrLogging;
    }

    //Check time sent in trace_c. Use this time instead of querying for the meter clock.
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
     * @return properties
     */
    private MTU155Properties getProperties() {
        return properties;
    }

    /**
     * @return logger
     */
    private Logger getLogger() {
        return logger;
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

    public Date getMeterClock() {
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
        pd.setIntervalDatas(getIntervalDatasFromResponse(response));
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


    public MeterAmrLogging getMeterAmrLogging() {
        if (meterAmrLogging == null) {
            meterAmrLogging = new MeterAmrLogging();
        }
        return meterAmrLogging;
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
}
