package com.energyict.genericprotocolimpl.elster.ctr.profile;

import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.elster.ctr.MTU155Properties;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;
import com.energyict.genericprotocolimpl.elster.ctr.structure.Trace_CQueryResponseStructure;
import com.energyict.genericprotocolimpl.elster.ctr.util.CTRObjectInfo;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Rtu;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.ProfileData;

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
    private Logger logger;
    private TimeZone timeZone;
    private Trace_CQueryResponseStructure response;
    private boolean fetchTotals;

    public ProfileChannelForSms(Logger logger, MTU155Properties properties, Channel meterChannel, Trace_CQueryResponseStructure response, TimeZone timeZone, boolean fetchTotals) {
        this.properties = properties;
        this.meterChannel = meterChannel;
        this.logger = logger;
        this.response = response;
        this.timeZone = timeZone;
        this.fetchTotals = fetchTotals;
    }

    /**
     * Gets the time(a date object) from the trace_c values
     *
     * @param values: the trace_c values
     * @return the date object with the time, as sent in the trace_c values
     */
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

    /**
     * Checks if the hours / minutes have an overflow. This indicates that a time shift is in progress.
     *
     * @param date: the date that needs to be checked
     * @return the real date without the overflow
     */
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

    /**
     * @return the profile data
     * @throws CTRException
     */
    public ProfileData getProfileData() throws CTRException {
        if (getChannelObjectId() == null) {
            getLogger().warning("No channel config found for channel with loadProfileIndex [" + getChannelIndex() + "]");
            return new ProfileData();
        }

        ProfileData pd = new ProfileData();
        pd.setChannelInfos(getChannelInfos());
        if (isFetchTotals()) {
            pd.setIntervalDatas(new TraceCProfileParser(response, getDeviceTimeZone()).getIntervalDataForTotalizer());
        } else {
            pd.setIntervalDatas(new TraceCProfileParser(response, getDeviceTimeZone()).getIntervalData());
        }

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

    public boolean isFetchTotals() {
        return this.fetchTotals;
    }
}